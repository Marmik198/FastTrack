import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  TableContainer,
  TablePagination,
  Stack,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
  IconButton,
  Grid,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import apiClient from '../../api/apiClient.js';

const emptyForm = {
  firstName: '', lastName: '', motherName: '', fatherName: '',
  mobile: '', alternateMobile: '', email: '', age: '', dateOfBirth: '', address: '',
};

export default function ChildrenPage() {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    apiClient
      .get('/api/admin/children', { params: { search, page, size: rowsPerPage } })
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load children'))
      .finally(() => setLoading(false));
  }, [search, page, rowsPerPage]);

  useEffect(load, [load]);

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setFormError('');
    setDialogOpen(true);
  };

  const openEdit = (child) => {
    setEditingId(child.id);
    setForm({
      firstName: child.firstName || '',
      lastName: child.lastName || '',
      motherName: child.motherName || '',
      fatherName: child.fatherName || '',
      mobile: child.mobile || '',
      alternateMobile: child.alternateMobile || '',
      email: child.email || '',
      age: child.age || '',
      dateOfBirth: child.dateOfBirth || '',
      address: child.address || '',
    });
    setFormError('');
    setDialogOpen(true);
  };

  const handleFieldChange = (field) => (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));

  const handleSave = async () => {
    setSaving(true);
    setFormError('');
    const payload = { ...form, age: form.age ? Number(form.age) : null };
    try {
      if (editingId) {
        await apiClient.put(`/api/admin/children/${editingId}`, payload);
      } else {
        await apiClient.post('/api/admin/children', payload);
      }
      setDialogOpen(false);
      load();
    } catch (err) {
      setFormError(err?.response?.data?.error || 'Failed to save');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h5">Children Master</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
          Add Child
        </Button>
      </Stack>

      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <TextField
          label="Search by name"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);
          }}
          fullWidth
        />
      </Paper>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Paper variant="outlined">
        {loading ? (
          <Box sx={{ p: 4, textAlign: 'center' }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Mobile</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Age</TableCell>
                    <TableCell>Father</TableCell>
                    <TableCell>Mother</TableCell>
                    <TableCell align="right">Edit</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(data.content || []).map((c) => (
                    <TableRow key={c.id} hover>
                      <TableCell>{c.firstName} {c.lastName}</TableCell>
                      <TableCell>{c.mobile}</TableCell>
                      <TableCell>{c.email}</TableCell>
                      <TableCell>{c.age}</TableCell>
                      <TableCell>{c.fatherName}</TableCell>
                      <TableCell>{c.motherName}</TableCell>
                      <TableCell align="right">
                        <IconButton size="small" onClick={() => openEdit(c)}>
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                  {(data.content || []).length === 0 && (
                    <TableRow>
                      <TableCell colSpan={7} align="center">No children found.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              component="div"
              count={data.totalElements || 0}
              page={page}
              onPageChange={(e, newPage) => setPage(newPage)}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={(e) => { setRowsPerPage(Number(e.target.value)); setPage(0); }}
              rowsPerPageOptions={[10, 25, 50]}
            />
          </>
        )}
      </Paper>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Child' : 'Add Child'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={6}>
              <TextField label="First Name" value={form.firstName} onChange={handleFieldChange('firstName')} fullWidth required />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Last Name" value={form.lastName} onChange={handleFieldChange('lastName')} fullWidth required />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Father Name" value={form.fatherName} onChange={handleFieldChange('fatherName')} fullWidth />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Mother Name" value={form.motherName} onChange={handleFieldChange('motherName')} fullWidth />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Mobile" value={form.mobile} onChange={handleFieldChange('mobile')} fullWidth required />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Alternate Mobile" value={form.alternateMobile} onChange={handleFieldChange('alternateMobile')} fullWidth />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Email" type="email" value={form.email} onChange={handleFieldChange('email')} fullWidth />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Age" type="number" value={form.age} onChange={handleFieldChange('age')} fullWidth />
            </Grid>
            <Grid item xs={6}>
              <TextField
                label="Date of Birth"
                type="date"
                value={form.dateOfBirth}
                onChange={handleFieldChange('dateOfBirth')}
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Address" value={form.address} onChange={handleFieldChange('address')} fullWidth multiline minRows={2} />
            </Grid>
          </Grid>
          {formError && <Alert severity="error" sx={{ mt: 2 }}>{formError}</Alert>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>
            {saving ? <CircularProgress size={20} color="inherit" /> : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
