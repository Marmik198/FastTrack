import React, { useState, useEffect, useCallback } from 'react';
import {
  Box, Typography, Paper, Button, Table, TableHead, TableBody, TableRow, TableCell,
  TableContainer, TablePagination, Stack, Dialog, DialogTitle, DialogContent, DialogActions,
  Alert, CircularProgress, Chip, Grid, TextField, Select, MenuItem, FormControl, InputLabel,
  FormControlLabel, Checkbox, Radio, RadioGroup,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import apiClient from '../../api/apiClient.js';
import ChildAutocomplete from '../../components/ChildAutocomplete.jsx';

export default function CampaignResponsesPage() {
  const [campaigns, setCampaigns] = useState([]);
  const [campaignFilter, setCampaignFilter] = useState('all');
  const [pendingOnly, setPendingOnly] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [manualOpen, setManualOpen] = useState(false);
  const [manualChild, setManualChild] = useState(null);
  const [manualCampaignId, setManualCampaignId] = useState('');
  const [manualCarryForward, setManualCarryForward] = useState('true');
  const [manualLink, setManualLink] = useState('');
  const [manualQuantity, setManualQuantity] = useState('');
  const [manualRemarks, setManualRemarks] = useState('');
  const [manualEmail, setManualEmail] = useState('');
  const [manualSaving, setManualSaving] = useState(false);
  const [manualError, setManualError] = useState('');

  const [approveTarget, setApproveTarget] = useState(null);
  const [approveAmount, setApproveAmount] = useState('');
  const [approveItem, setApproveItem] = useState('');
  const [approveSaving, setApproveSaving] = useState(false);
  const [approveError, setApproveError] = useState('');

  useEffect(() => {
    apiClient.get('/api/admin/campaigns', { params: { size: 100 } }).then((res) => setCampaigns(res.data.content || []));
  }, []);

  const load = useCallback(() => {
    setLoading(true);
    apiClient
      .get('/api/admin/campaign-responses', {
        params: {
          campaignId: campaignFilter !== 'all' ? campaignFilter : undefined,
          pendingOnly,
          page, size: rowsPerPage,
        },
      })
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load responses'))
      .finally(() => setLoading(false));
  }, [campaignFilter, pendingOnly, page, rowsPerPage]);

  useEffect(load, [load]);

  const resetManualForm = () => {
    setManualChild(null);
    setManualCampaignId('');
    setManualCarryForward('true');
    setManualLink('');
    setManualQuantity('');
    setManualRemarks('');
    setManualEmail('');
    setManualError('');
  };

  const handleManualSubmit = async () => {
    if (!manualChild || !manualCampaignId) {
      setManualError('Please select a child and a campaign');
      return;
    }
    setManualSaving(true);
    setManualError('');
    const carryForward = manualCarryForward === 'true';
    try {
      await apiClient.post('/api/admin/campaign-responses/manual', {
        childId: manualChild.id,
        campaignId: manualCampaignId,
        carryForwardFlag: carryForward,
        link: carryForward ? null : manualLink.trim(),
        quantity: carryForward ? null : Number(manualQuantity),
        remarks: manualRemarks.trim() || null,
        email: manualEmail.trim() || null,
      });
      setManualOpen(false);
      resetManualForm();
      load();
    } catch (err) {
      setManualError(err?.response?.data?.error || 'Failed to save');
    } finally {
      setManualSaving(false);
    }
  };

  const openApprove = (response) => {
    setApproveTarget(response);
    setApproveAmount('');
    setApproveItem('');
    setApproveError('');
  };

  const handleApprove = async () => {
    if (!approveAmount || !approveItem.trim()) {
      setApproveError('Please enter both the points amount and item name');
      return;
    }
    setApproveSaving(true);
    setApproveError('');
    try {
      await apiClient.post(`/api/admin/campaign-responses/${approveTarget.id}/approve`, {
        amount: Number(approveAmount),
        item: approveItem.trim(),
      });
      setApproveTarget(null);
      load();
    } catch (err) {
      setApproveError(err?.response?.data?.error || 'Failed to approve');
    } finally {
      setApproveSaving(false);
    }
  };

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h5">Campaign Responses</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => { resetManualForm(); setManualOpen(true); }}>
          Create Manual Response
        </Button>
      </Stack>

      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={5}>
            <FormControl fullWidth>
              <InputLabel>Campaign</InputLabel>
              <Select
                value={campaignFilter}
                label="Campaign"
                onChange={(e) => { setCampaignFilter(e.target.value); setPage(0); }}
              >
                <MenuItem value="all">All campaigns</MenuItem>
                {campaigns.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={5}>
            <FormControlLabel
              control={<Checkbox checked={pendingOnly} onChange={(e) => { setPendingOnly(e.target.checked); setPage(0); }} />}
              label="Pending approval only (redeem responses not yet approved)"
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
                    <TableCell>Campaign</TableCell>
                    <TableCell>Choice</TableCell>
                    <TableCell>Link</TableCell>
                    <TableCell align="right">Qty</TableCell>
                    <TableCell>Remarks</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell align="right">Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(data.content || []).map((r) => (
                    <TableRow key={r.id} hover>
                      <TableCell>{r.childName}</TableCell>
                      <TableCell>{r.campaignName}</TableCell>
                      <TableCell>
                        <Chip size="small" label={r.carryForwardFlag ? 'Carry Forward' : 'Redeem'} color={r.carryForwardFlag ? 'default' : 'primary'} />
                      </TableCell>
                      <TableCell sx={{ maxWidth: 160, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {r.link ? <a href={r.link} target="_blank" rel="noopener noreferrer">View</a> : '—'}
                      </TableCell>
                      <TableCell align="right">{r.quantity ?? '—'}</TableCell>
                      <TableCell sx={{ maxWidth: 140, overflow: 'hidden', textOverflow: 'ellipsis' }}>{r.remarks || '—'}</TableCell>
                      <TableCell>{r.email}</TableCell>
                      <TableCell>
                        {r.carryForwardFlag ? (
                          <Chip size="small" label="N/A" />
                        ) : r.approved ? (
                          <Chip size="small" icon={<CheckCircleIcon />} label="Approved" color="success" />
                        ) : (
                          <Chip size="small" label="Pending" color="warning" />
                        )}
                      </TableCell>
                      <TableCell align="right">
                        {!r.carryForwardFlag && !r.approved && r.link && (
                          <Button size="small" variant="outlined" onClick={() => openApprove(r)}>Approve</Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                  {(data.content || []).length === 0 && (
                    <TableRow><TableCell colSpan={9} align="center">No responses found.</TableCell></TableRow>
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

      {/* Manual response creation dialog */}
      <Dialog open={manualOpen} onClose={() => setManualOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create Manual Response</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <ChildAutocomplete value={manualChild} onChange={setManualChild} label="Child" />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Campaign</InputLabel>
                <Select value={manualCampaignId} label="Campaign" onChange={(e) => setManualCampaignId(e.target.value)}>
                  {campaigns.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <RadioGroup row value={manualCarryForward} onChange={(e) => setManualCarryForward(e.target.value)}>
                <FormControlLabel value="true" control={<Radio />} label="Carry Forward" />
                <FormControlLabel value="false" control={<Radio />} label="Redeem" />
              </RadioGroup>
            </Grid>
            {manualCarryForward === 'false' && (
              <>
                <Grid item xs={8}>
                  <TextField label="Product Link" value={manualLink} onChange={(e) => setManualLink(e.target.value)} fullWidth />
                </Grid>
                <Grid item xs={4}>
                  <TextField label="Quantity" type="number" value={manualQuantity} onChange={(e) => setManualQuantity(e.target.value)} fullWidth />
                </Grid>
              </>
            )}
            <Grid item xs={12}>
              <TextField label="Remarks" value={manualRemarks} onChange={(e) => setManualRemarks(e.target.value)} fullWidth multiline minRows={2} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Email" value={manualEmail} onChange={(e) => setManualEmail(e.target.value)} fullWidth />
            </Grid>
          </Grid>
          {manualError && <Alert severity="error" sx={{ mt: 2 }}>{manualError}</Alert>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setManualOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleManualSubmit} disabled={manualSaving}>
            {manualSaving ? <CircularProgress size={20} color="inherit" /> : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Approve into redemption dialog */}
      <Dialog open={!!approveTarget} onClose={() => setApproveTarget(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Approve Redemption</DialogTitle>
        <DialogContent>
          {approveTarget && (
            <Stack spacing={2} sx={{ mt: 0.5 }}>
              <Typography variant="body2" color="text.secondary">
                Approving {approveTarget.childName}'s response — this will burn the points amount you enter below.
              </Typography>
              <TextField label="Points Amount to Burn" type="number" value={approveAmount} onChange={(e) => setApproveAmount(e.target.value)} fullWidth required />
              <TextField label="Item Name" value={approveItem} onChange={(e) => setApproveItem(e.target.value)} fullWidth required />
              {approveError && <Alert severity="error">{approveError}</Alert>}
            </Stack>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setApproveTarget(null)}>Cancel</Button>
          <Button variant="contained" onClick={handleApprove} disabled={approveSaving}>
            {approveSaving ? <CircularProgress size={20} color="inherit" /> : 'Approve'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
