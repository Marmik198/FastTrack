import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Paper, Box, Typography, TextField, Button, Alert, Stack, CircularProgress } from '@mui/material';
import LockPersonIcon from '@mui/icons-material/LockPerson';
import apiClient from '../../api/apiClient.js';

export default function AdminLoginPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrorMsg('');
    setLoading(true);
    try {
      const res = await apiClient.post('/api/admin/auth/login', { username, password });
      localStorage.setItem('ft_admin_token', res.data.token);
      navigate('/admin/children');
    } catch (err) {
      setErrorMsg(err?.response?.data?.error || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default', display: 'flex', alignItems: 'center' }}>
      <Container maxWidth="xs">
        <Stack alignItems="center" spacing={1} sx={{ mb: 3 }}>
          <LockPersonIcon color="secondary" sx={{ fontSize: 40 }} />
          <Typography variant="h5" color="secondary.dark">
            FastTrack Admin
          </Typography>
          <Typography variant="body2" color="text.secondary" textAlign="center">
            Karma Yuva Group - Point Redemption Admin Panel
          </Typography>
        </Stack>

        <Paper elevation={0} sx={{ p: 4, border: '1px solid', borderColor: 'divider' }}>
          <Box component="form" onSubmit={handleSubmit}>
            <Stack spacing={2}>
              <TextField
                label="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                fullWidth
                autoFocus
              />
              <TextField
                label="Password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                fullWidth
              />
              {errorMsg && <Alert severity="error">{errorMsg}</Alert>}
              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={loading}
                endIcon={loading ? <CircularProgress size={18} color="inherit" /> : null}
              >
                {loading ? 'Signing in...' : 'Sign In'}
              </Button>
            </Stack>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
}
