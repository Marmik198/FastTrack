import React, { useState, useEffect, useCallback } from 'react';
import {
  Box, Typography, Paper, TextField, Table, TableHead, TableBody, TableRow, TableCell,
  TableContainer, TablePagination, Alert, CircularProgress, Grid, Select, MenuItem, FormControl, InputLabel,
} from '@mui/material';
import apiClient from '../../api/apiClient.js';

export default function RedemptionsPage() {
  const [campaigns, setCampaigns] = useState([]);
  const [campaignFilter, setCampaignFilter] = useState('all');
  const [childName, setChildName] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    apiClient.get('/api/admin/campaigns', { params: { size: 100 } }).then((res) => setCampaigns(res.data.content || []));
  }, []);

  const load = useCallback(() => {
    setLoading(true);
    apiClient
      .get('/api/admin/redemptions', {
        params: { campaignId: campaignFilter !== 'all' ? campaignFilter : undefined, childName, page, size: rowsPerPage },
      })
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load redemptions'))
      .finally(() => setLoading(false));
  }, [campaignFilter, childName, page, rowsPerPage]);

  useEffect(load, [load]);

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Redemptions</Typography>

      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Campaign</InputLabel>
              <Select value={campaignFilter} label="Campaign" onChange={(e) => { setCampaignFilter(e.target.value); setPage(0); }}>
                <MenuItem value="all">All campaigns</MenuItem>
                {campaigns.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Search by child name"
              value={childName}
              onChange={(e) => { setChildName(e.target.value); setPage(0); }}
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
                    <TableCell>Date</TableCell>
                    <TableCell>Child</TableCell>
                    <TableCell align="right">Amount</TableCell>
                    <TableCell>Item</TableCell>
                    <TableCell>Link</TableCell>
                    <TableCell align="right">Qty</TableCell>
                    <TableCell>Approved By</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(data.content || []).map((r) => (
                    <TableRow key={r.id} hover>
                      <TableCell>{r.date}</TableCell>
                      <TableCell>{r.childName}</TableCell>
                      <TableCell align="right">{r.amount}</TableCell>
                      <TableCell>{r.item}</TableCell>
                      <TableCell sx={{ maxWidth: 160, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {r.link ? <a href={r.link} target="_blank" rel="noopener noreferrer">View</a> : '—'}
                      </TableCell>
                      <TableCell align="right">{r.quantity ?? '—'}</TableCell>
                      <TableCell>{r.createdBy}</TableCell>
                    </TableRow>
                  ))}
                  {(data.content || []).length === 0 && (
                    <TableRow><TableCell colSpan={7} align="center">No redemptions found.</TableCell></TableRow>
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
