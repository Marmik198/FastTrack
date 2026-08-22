import React, { useState, useEffect } from 'react';
import { Autocomplete, TextField, CircularProgress } from '@mui/material';
import apiClient from '../api/apiClient.js';

function useDebouncedValue(value, delayMs) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const handle = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(handle);
  }, [value, delayMs]);
  return debounced;
}

export default function ChildAutocomplete({ value, onChange, label = 'Search child by name' }) {
  const [inputValue, setInputValue] = useState('');
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const debouncedInput = useDebouncedValue(inputValue, 300);

  useEffect(() => {
    let active = true;
    setLoading(true);
    apiClient
      .get('/api/admin/children', { params: { search: debouncedInput, size: 15 } })
      .then((res) => {
        if (active) setOptions(res.data.content || []);
      })
      .catch(() => {
        if (active) setOptions([]);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [debouncedInput]);

  return (
    <Autocomplete
      options={options}
      loading={loading}
      value={value}
      onChange={(event, newValue) => onChange(newValue)}
      onInputChange={(event, newInputValue) => setInputValue(newInputValue)}
      getOptionLabel={(option) => (option ? `${option.firstName} ${option.lastName}` : '')}
      isOptionEqualToValue={(option, val) => option.id === val.id}
      renderInput={(params) => (
        <TextField
          {...params}
          label={label}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading ? <CircularProgress color="inherit" size={18} /> : null}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
      noOptionsText="No matching child found"
    />
  );
}
