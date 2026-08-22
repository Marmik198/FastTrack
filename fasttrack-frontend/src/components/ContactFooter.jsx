import React from 'react';
import { Box, Typography, Stack, Link } from '@mui/material';
import PhoneIcon from '@mui/icons-material/Phone';
import EmailIcon from '@mui/icons-material/Email';
import { useTranslation } from '../i18n/I18nContext.jsx';

// Update these with your organization's real contact people/details.
const CONTACTS = [
  { name: 'Karma Yuva Group Office', phone: '+91 98765 43210', email: 'contact@karmayuvagroup.org' },
];

export default function ContactFooter() {
  const { t } = useTranslation();
  return (
    <Box sx={{ mt: 4, pt: 3, borderTop: '1px solid', borderColor: 'divider' }}>
      <Typography variant="subtitle2" color="text.secondary" gutterBottom>
        {t('contactTitle')}
      </Typography>
      <Stack spacing={1}>
        {CONTACTS.map((c) => (
          <Box key={c.name}>
            <Typography variant="body2" fontWeight={600}>
              {c.name}
            </Typography>
            <Stack direction="row" spacing={2} sx={{ mt: 0.5 }}>
              <Stack direction="row" spacing={0.5} alignItems="center">
                <PhoneIcon fontSize="small" color="action" />
                <Link href={`tel:${c.phone}`} underline="hover" color="text.secondary" variant="body2">
                  {c.phone}
                </Link>
              </Stack>
              <Stack direction="row" spacing={0.5} alignItems="center">
                <EmailIcon fontSize="small" color="action" />
                <Link href={`mailto:${c.email}`} underline="hover" color="text.secondary" variant="body2">
                  {c.email}
                </Link>
              </Stack>
            </Stack>
          </Box>
        ))}
      </Stack>
    </Box>
  );
}
