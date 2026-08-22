import React, { useState, useEffect, useCallback } from 'react';
import {
  Box, Typography, Paper, TextField, Button, Table, TableHead, TableBody, TableRow, TableCell,
  TableContainer, TablePagination, Stack, Alert, CircularProgress, Grid,
} from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import apiClient from '../../api/apiClient.js';
import ChildAutocomplete from '../../components/ChildAutocomplete.jsx';

export default function AccrualsPage() {
  const [filterChild, setFilterChild] = useState(null);
  const [filterMonth, setFilterMonth] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [manualChild, setManualChild] = useState(null);
  const [manualMonth, setManualMonth] = useState('');
  const [manualFrequency, setManualFrequency] = useState('');
  const [savingManual, setSavingManual] = useState(false);
  const [manualError, setManualError] = useState('');

  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState(null);
  const [uploadError, setUploadError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    apiClient
      .get('/api/admin/accruals', {
        params: { childId: filterChild?.id, month: filterMonth, page, size: rowsPerPage },
      })
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load accruals'))
      .finally(() => setLoading(false));
  }, [filterChild, filterMonth, page, rowsPerPage]);

  useEffect(load, [load]);

  const handleManualAdd = async () => {
    if (!manualChild || !manualMonth.trim() || !manualFrequency) return;
    setSavingManual(true);
    setManualError('');
    try {
      await apiClient.post('/api/admin/accruals', {
        childId: manualChild.id,
        month: manualMonth.trim(),
        frequency: Number(manualFrequency),
      });
      setManualChild(null);
      setManualMonth('');
      setManualFrequency('');
      load();
    } catch (err) {
      setManualError(err?.response?.data?.error || 'Failed to save');
    } finally {
      setSavingManual(false);
    }
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;
    setUploading(true);
    setUploadError('');
    setUploadResult(null);
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await apiClient.post('/api/admin/accruals/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setUploadResult(res.data);
      load();
    } catch (err) {
      setUploadError(err?.response?.data?.error || 'Failed to upload file');
    } finally {
      setUploading(false);
      event.target.value = '';
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Accrual Transactions</Typography>

      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <Typography variant="subtitle1" gutterBottom>Upload monthly points sheet</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Accepts .csv, .txt, or .xlsx. Header row needs <code>month</code>, <code>frequency</code>, and either{' '}
          <code>childId</code> or <code>childName</code> columns.
        </Typography>
        <Button component="label" variant="contained" startIcon={<UploadFileIcon />} disabled={uploading}>
          {uploading ? 'Uploading...' : 'Choose file'}
          <input type="file" accept=".csv,.txt,.xlsx,.xls" hidden onChange={handleFileUpload} />
        </Button>
        {uploadError && <Alert severity="error" sx={{ mt: 2 }}>{uploadError}</Alert>}
        {uploadResult && (
          <Alert severity="success" sx={{ mt: 2 }}>
            Processed {uploadResult.rowsProcessed} rows, {uploadResult.accrualsCreated} accruals created.
            {uploadResult.errors.length > 0 && (
              <Box sx={{ mt: 1 }}>
                {uploadResult.errors.map((e, i) => <Typography key={i} variant="body2">{e}</Typography>)}
              </Box>
            )}
          </Alert>
        )}
      </Paper>

      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <Typography variant="subtitle1" gutterBottom>Add a single accrual manually</Typography>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={4}>
            <ChildAutocomplete value={manualChild} onChange={setManualChild} label="Child" />
          </Grid>
          <Grid item xs={6} sm={3}>
            <TextField label="Month (YYYY-MM)" value={manualMonth} onChange={(e) => setManualMonth(e.target.value)} fullWidth />
          </Grid>
          <Grid item xs={6} sm={3}>
            <TextField label="Frequency" type="number" value={manualFrequency} onChange={(e) => setManualFrequency(e.target.value)} fullWidth />
          </Grid>
          <Grid item xs={12} sm={2}>
            <Button variant="contained" onClick={handleManualAdd} disabled={savingManual} fullWidth>
              {savingManual ? <CircularProgress size={20} color="inherit" /> : 'Add'}
            </Button>
          </Grid>
        </Grid>
        {manualError && <Alert severity="error" sx={{ mt: 2 }}>{manualError}</Alert>}
      </Paper>

      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <ChildAutocomplete value={filterChild} onChange={(v) => { setFilterChild(v); setPage(0); }} label="Filter by child" />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Filter by month (YYYY-MM)"
              value={filterMonth}
              onChange={(e) => { setFilterMonth(e.target.value); setPage(0); }}
              fullWidth
            />
          </Grid>
        </Grid>
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
                    <TableCell>Child</TableCell>
                    <TableCell>Month</TableCell>
                    <TableCell align="right">Frequency</TableCell>
                    <TableCell>Created</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(data.content || []).map((a) => (
                    <TableRow key={a.id} hover>
                      <TableCell>{a.childName}</TableCell>
                      <TableCell>{a.month}</TableCell>
                      <TableCell align="right">{a.frequency}</TableCell>
                      <TableCell>{new Date(a.creationDate).toLocaleString()}</TableCell>
                    </TableRow>
                  ))}
                  {(data.content || []).length === 0 && (
                    <TableRow><TableCell colSpan={4} align="center">No accruals found.</TableCell></TableRow>
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
    </Box>
  );
}
