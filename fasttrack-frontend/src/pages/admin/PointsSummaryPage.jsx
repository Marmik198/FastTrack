import React, { useState, useEffect, useCallback } from 'react';
import {
  Box, Typography, Paper, TextField, Table, TableHead, TableBody, TableRow, TableCell,
  TableContainer, TablePagination, Alert, CircularProgress,
} from '@mui/material';
import apiClient from '../../api/apiClient.js';

export default function PointsSummaryPage() {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    apiClient
      .get('/api/admin/points/summary', { params: { search, page, size: rowsPerPage } })
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load points summary'))
      .finally(() => setLoading(false));
  }, [search, page, rowsPerPage]);

  useEffect(load, [load]);

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Points Summary</Typography>

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
                    <TableCell>Mobile</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell align="right">Total Accrued</TableCell>
                    <TableCell align="right">Total Burnt</TableCell>
                    <TableCell align="right">Available</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(data.content || []).map((s) => (
                    <TableRow key={s.childId} hover>
                      <TableCell>{s.firstName} {s.lastName}</TableCell>
                      <TableCell>{s.mobile}</TableCell>
                      <TableCell>{s.email}</TableCell>
                      <TableCell align="right">{s.totalAccrued}</TableCell>
                      <TableCell align="right">{s.totalBurnt}</TableCell>
                      <TableCell align="right"><strong>{s.availablePoints}</strong></TableCell>
                    </TableRow>
                  ))}
                  {(data.content || []).length === 0 && (
                    <TableRow><TableCell colSpan={6} align="center">No children found.</TableCell></TableRow>
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
