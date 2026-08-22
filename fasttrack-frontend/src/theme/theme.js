import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#E07A1F',
      dark: '#B85E12',
      light: '#F0A868',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#7A1F3D',
      dark: '#5C1730',
      light: '#9C4361',
      contrastText: '#FFFFFF',
    },
    success: { main: '#2E7D4F' },
    warning: { main: '#C77700' },
    error: { main: '#C2362B' },
    background: {
      default: '#FBF6EE',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#2B2018',
      secondary: '#6B5B4D',
    },
  },
  typography: {
    fontFamily:
      '"Inter", "Noto Sans Gujarati", "Noto Sans Devanagari", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: { fontFamily: '"Baloo 2", "Noto Sans Gujarati", "Noto Sans Devanagari", sans-serif', fontWeight: 700 },
    h2: { fontFamily: '"Baloo 2", "Noto Sans Gujarati", "Noto Sans Devanagari", sans-serif', fontWeight: 700 },
    h3: { fontFamily: '"Baloo 2", "Noto Sans Gujarati", "Noto Sans Devanagari", sans-serif', fontWeight: 700 },
    h4: { fontFamily: '"Baloo 2", "Noto Sans Gujarati", "Noto Sans Devanagari", sans-serif', fontWeight: 600 },
    h5: { fontFamily: '"Baloo 2", "Noto Sans Gujarati", "Noto Sans Devanagari", sans-serif', fontWeight: 600 },
    h6: { fontFamily: '"Baloo 2", "Noto Sans Gujarati", "Noto Sans Devanagari", sans-serif', fontWeight: 600 },
    button: { fontWeight: 600, textTransform: 'none' },
  },
  shape: { borderRadius: 12 },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 10, paddingTop: 10, paddingBottom: 10 },
        containedPrimary: { boxShadow: '0 4px 14px rgba(224, 122, 31, 0.35)' },
      },
    },
    MuiPaper: {
      styleOverrides: { rounded: { borderRadius: 16 } },
    },
    MuiAppBar: {
      styleOverrides: { root: { boxShadow: '0 2px 10px rgba(0,0,0,0.08)' } },
    },
    MuiChip: {
      styleOverrides: { root: { fontWeight: 600 } },
    },
  },
});

export default theme;
