import React, { useState } from 'react';
import {
  Container,
  Paper,
  Box,
  Typography,
  TextField,
  Button,
  Radio,
  RadioGroup,
  FormControlLabel,
  FormControl,
  FormLabel,
  Alert,
  Chip,
  CircularProgress,
  Divider,
  Stack,
} from '@mui/material';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import apiClient from '../../api/apiClient.js';
import ContactFooter from '../../components/ContactFooter.jsx';
import { useTranslation, LanguageSwitcher } from '../../i18n/I18nContext.jsx';

const initialFormState = {
  carryForwardFlag: 'true',
  link: '',
  quantity: '',
  remarks: '',
  email: '',
};

export default function NameLookupFormPage() {
  const { t } = useTranslation();

  const [name, setName] = useState('');
  const [lookupLoading, setLookupLoading] = useState(false);
  const [lookupResult, setLookupResult] = useState(null); // NameLookupResultDto or null
  const [lookupError, setLookupError] = useState('');

  const [form, setForm] = useState(initialFormState);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  const [doneStatus, setDoneStatus] = useState(null); // 'submitted' | 'alreadySubmitted'

  const handleLookup = async (event) => {
    event.preventDefault();
    setLookupError('');
    setLookupLoading(true);
    try {
      const res = await apiClient.post('/api/public/lookup', { name: name.trim() });
      if (res.data.status === 'ALREADY_SUBMITTED') {
        setDoneStatus('alreadySubmitted');
      } else if (res.data.status === 'OK') {
        setLookupResult(res.data);
        setForm((prev) => ({ ...prev, email: res.data.email || '' }));
      } else {
        setLookupResult(res.data); // CLOSED or NOT_FOUND
      }
    } catch (err) {
      setLookupError(err?.response?.data?.error || t('errorGeneric'));
    } finally {
      setLookupLoading(false);
    }
  };

  const handleFieldChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitError('');

    const carryForward = form.carryForwardFlag === 'true';
    const payload = {
      childId: lookupResult.childId,
      campaignId: lookupResult.campaignId,
      carryForwardFlag: carryForward,
      link: carryForward ? null : form.link.trim(),
      quantity: carryForward ? null : Number(form.quantity),
      remarks: form.remarks.trim() || null,
      email: form.email.trim(),
    };

    setSubmitting(true);
    try {
      await apiClient.post('/api/public/campaign-responses', payload);
      setDoneStatus('submitted');
    } catch (err) {
      setSubmitError(err?.response?.data?.error || t('errorGeneric'));
    } finally {
      setSubmitting(false);
    }
  };

  const resetToLookup = () => {
    setLookupResult(null);
    setName('');
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default', py: { xs: 4, sm: 8 } }}>
      <Container maxWidth="sm">
        <Stack direction="row" justifyContent="flex-end" sx={{ mb: 2 }}>
          <LanguageSwitcher />
        </Stack>

        <Stack alignItems="center" spacing={1} sx={{ mb: 4, textAlign: 'center' }}>
          <VolunteerActivismIcon color="secondary" sx={{ fontSize: 44 }} />
          <Typography variant="h4" color="secondary.dark">
            {t('appName')}
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            {t('tagline')}
          </Typography>
        </Stack>

        <Paper elevation={0} sx={{ p: { xs: 3, sm: 4 }, border: '1px solid', borderColor: 'divider' }}>
          {/* Shared static screen for both "just submitted" and "already submitted" */}
          {doneStatus && (
            <Stack spacing={2} alignItems="center" sx={{ textAlign: 'center', py: 3 }}>
              <CheckCircleIcon color="success" sx={{ fontSize: 56 }} />
              <Typography variant="h6">
                {doneStatus === 'submitted' ? t('thankYouTitle') : t('alreadySubmittedTitle')}
              </Typography>
              <Typography color="text.secondary">
                {doneStatus === 'submitted' ? t('thankYouMessage') : t('alreadySubmittedMessage')}
              </Typography>
              <ContactFooter />
            </Stack>
          )}

          {/* Stage 1: name entry (also handles CLOSED / NOT_FOUND results) */}
          {!doneStatus && !lookupResult && (
            <Box component="form" onSubmit={handleLookup}>
              <Stack spacing={3}>
                <TextField
                  label={t('nameLabel')}
                  placeholder={t('namePlaceholder')}
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                  fullWidth
                  autoFocus
                  helperText={t('englishOnlyNote')}
                />
                {lookupError && <Alert severity="error">{lookupError}</Alert>}
                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  disabled={lookupLoading}
                  endIcon={lookupLoading ? <CircularProgress size={18} color="inherit" /> : null}
                >
                  {lookupLoading ? t('loading') : t('findMeButton')}
                </Button>
              </Stack>
            </Box>
          )}

          {/* CLOSED result */}
          {!doneStatus && lookupResult && lookupResult.status === 'CLOSED' && (
            <Stack spacing={2}>
              <Alert severity="info">
                <Typography fontWeight={600}>{t('closedTitle')}</Typography>
                {t('closedMessage')}
              </Alert>
              <ContactFooter />
            </Stack>
          )}

          {/* NOT_FOUND result */}
          {!doneStatus && lookupResult && lookupResult.status === 'NOT_FOUND' && (
            <Stack spacing={2}>
              <Alert severity="warning">
                <Typography fontWeight={600}>{t('notFoundTitle')}</Typography>
                {t('notFoundMessage')}
              </Alert>
              <Button variant="outlined" onClick={resetToLookup}>
                {t('tryAgain')}
              </Button>
              <ContactFooter />
            </Stack>
          )}

          {/* OK result: show child info + the actual submission form */}
          {!doneStatus && lookupResult && lookupResult.status === 'OK' && (
            <Box component="form" onSubmit={handleSubmit}>
              <Stack spacing={3}>
                <Chip color="primary" variant="outlined" label={`${t('campaign')}: ${lookupResult.campaignName}`} />

                <Alert severity="success" icon={false}>
                  <Typography variant="body2">
                    {t('greeting')} <strong>{lookupResult.firstName} {lookupResult.lastName}</strong>!
                  </Typography>
                  <Typography variant="body2">{t('mobile')}: {lookupResult.mobile}</Typography>
                  <Typography variant="body2">{t('email')}: {lookupResult.email}</Typography>
                  {lookupResult.address && (
                    <Typography variant="body2">{t('address')}: {lookupResult.address}</Typography>
                  )}
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    <strong>{t('availablePoints')}: {lookupResult.availablePoints}</strong>
                  </Typography>
                </Alert>

                <Divider />

                <FormControl>
                  <FormLabel>{t('chooseOption')}</FormLabel>
                  <RadioGroup value={form.carryForwardFlag} onChange={handleFieldChange('carryForwardFlag')}>
                    <FormControlLabel value="true" control={<Radio />} label={t('carryForward')} />
                    <FormControlLabel value="false" control={<Radio />} label={t('redeem')} />
                  </RadioGroup>
                </FormControl>

                {form.carryForwardFlag === 'false' && (
                  <Stack spacing={3}>
                    <TextField
                      label={t('productLink')}
                      placeholder="https://..."
                      value={form.link}
                      onChange={handleFieldChange('link')}
                      required
                      fullWidth
                      helperText={t('productLinkHelper')}
                    />
                    <TextField
                      label={t('quantity')}
                      type="number"
                      value={form.quantity}
                      onChange={handleFieldChange('quantity')}
                      required
                      fullWidth
                      inputProps={{ min: 1 }}
                    />
                  </Stack>
                )}

                <TextField
                  label={t('remarks')}
                  value={form.remarks}
                  onChange={handleFieldChange('remarks')}
                  fullWidth
                  multiline
                  minRows={2}
                />

                <TextField
                  label={t('emailField')}
                  type="email"
                  value={form.email}
                  onChange={handleFieldChange('email')}
                  required
                  fullWidth
                />

                {submitError && <Alert severity="error">{submitError}</Alert>}

                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  disabled={submitting}
                  endIcon={submitting ? <CircularProgress size={18} color="inherit" /> : null}
                >
                  {submitting ? t('submitting') : t('submit')}
                </Button>
              </Stack>
            </Box>
          )}
        </Paper>

        {!doneStatus && lookupResult && lookupResult.status === 'OK' && (
          <Box sx={{ mt: 3 }}>
            <ContactFooter />
          </Box>
        )}
      </Container>
    </Box>
  );
}
