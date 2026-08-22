import React, { useState, useEffect, useCallback } from 'react';
import {
  Box, Typography, Paper, TextField, Button, Table, TableHead, TableBody, TableRow, TableCell,
  TableContainer, TablePagination, Stack, Dialog, DialogTitle, DialogContent, DialogActions,
  Alert, CircularProgress, IconButton, Chip, Grid, Tooltip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import apiClient from '../../api/apiClient.js';

const emptyForm = { name: '', startDate: '', endDate: '', extendedEndDate: '' };

export default function CampaignsPage() {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copiedId, setCopiedId] = useState(null);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    apiClient
      .get('/api/admin/campaigns', { params: { search, page, size: rowsPerPage } })
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load campaigns'))
      .finally(() => setLoading(false));
  }, [search, page, rowsPerPage]);

  useEffect(load, [load]);

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setFormError('');
    setDialogOpen(true);
  };

  const openEdit = (campaign) => {
    setEditingId(campaign.id);
    setForm({
      name: campaign.name,
      startDate: campaign.startDate,
      endDate: campaign.endDate,
      extendedEndDate: campaign.extendedEndDate || '',
    });
    setFormError('');
    setDialogOpen(true);
  };

  const handleFieldChange = (field) => (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));

  const handleSave = async () => {
    setSaving(true);
    setFormError('');
    const payload = {
      name: form.name,
      startDate: form.startDate,
      endDate: form.endDate,
      extendedEndDate: form.extendedEndDate || null,
    };
    try {
      if (editingId) {
        await apiClient.put(`/api/admin/campaigns/${editingId}`, payload);
      } else {
        await apiClient.post('/api/admin/campaigns', payload);
      }
      setDialogOpen(false);
      load();
    } catch (err) {
      setFormError(err?.response?.data?.error || 'Failed to save');
    } finally {
      setSaving(false);
    }
  };

  const handleCopyLink = (campaign) => {
    navigator.clipboard.writeText(campaign.formLink);
    setCopiedId(campaign.id);
    setTimeout(() => setCopiedId(null), 1500);
  };

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h5">Campaigns</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
          New Campaign
        </Button>
      </Stack>

      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <TextField
          label="Search by name"
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          fullWidth
        />
      </Paper>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Paper variant="outlined">
        {loading ? (
          <Box sx={{ p: 4, textAlign: 'center' }}><CircularProgress /></Box>
        ) : (
          <>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Start</TableCell>
                    <TableCell>End</TableCell>
                    <TableCell>Extended End</TableCell>
                    <TableCell>Form Link</TableCell>
                    <TableCell align="right">Edit</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(data.content || []).map((c) => (
                    <TableRow key={c.id} hover>
                      <TableCell>{c.name}</TableCell>
                      <TableCell>
                        <Chip size="small" label={c.currentlyOpen ? 'Open' : 'Closed'} color={c.currentlyOpen ? 'success' : 'default'} />
                      </TableCell>
                      <TableCell>{c.startDate}</TableCell>
                      <TableCell>{c.endDate}</TableCell>
                      <TableCell>{c.extendedEndDate || '—'}</TableCell>
                      <TableCell>
                        <Tooltip title={copiedId === c.id ? 'Copied!' : 'Copy link'}>
                          <IconButton size="small" onClick={() => handleCopyLink(c)}>
                            <ContentCopyIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                      <TableCell align="right">
                        <IconButton size="small" onClick={() => openEdit(c)}>
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                  {(data.content || []).length === 0 && (
                    <TableRow><TableCell colSpan={7} align="center">No campaigns found.</TableCell></TableRow>
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
        <DialogTitle>{editingId ? 'Edit Campaign' : 'New Campaign'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField label="Campaign Name" value={form.name} onChange={handleFieldChange('name')} fullWidth required />
            </Grid>
            <Grid item xs={6}>
              <TextField
                label="Start Date" type="date" value={form.startDate} onChange={handleFieldChange('startDate')}
                fullWidth required InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                label="End Date" type="date" value={form.endDate} onChange={handleFieldChange('endDate')}
                fullWidth required InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Extended End Date (optional)" type="date" value={form.extendedEndDate}
                onChange={handleFieldChange('extendedEndDate')} fullWidth InputLabelProps={{ shrink: true }}
                helperText="Set this to extend a campaign past its original end date without losing that original date"
              />
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
