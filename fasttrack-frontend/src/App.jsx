import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import NameLookupFormPage from './pages/public/NameLookupFormPage.jsx';
import AdminLoginPage from './pages/admin/AdminLoginPage.jsx';
import AdminLayout from './pages/admin/AdminLayout.jsx';
import ChildrenPage from './pages/admin/ChildrenPage.jsx';
import AccrualsPage from './pages/admin/AccrualsPage.jsx';
import PointsSummaryPage from './pages/admin/PointsSummaryPage.jsx';
import CampaignsPage from './pages/admin/CampaignsPage.jsx';
import CampaignResponsesPage from './pages/admin/CampaignResponsesPage.jsx';
import RedemptionsPage from './pages/admin/RedemptionsPage.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

export default function App() {
  return (
    <Routes>
      {/* Public - the campaignId in the URL is for reference/bookmarking; the active
          campaign is always resolved server-side, so /form works standalone too */}
      <Route path="/" element={<NameLookupFormPage />} />
      <Route path="/form" element={<NameLookupFormPage />} />
      <Route path="/form/:campaignId" element={<NameLookupFormPage />} />

      <Route path="/admin/login" element={<AdminLoginPage />} />
      <Route
        path="/admin"
        element={
          <ProtectedRoute>
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/admin/children" replace />} />
        <Route path="children" element={<ChildrenPage />} />
        <Route path="accruals" element={<AccrualsPage />} />
        <Route path="points" element={<PointsSummaryPage />} />
        <Route path="campaigns" element={<CampaignsPage />} />
        <Route path="responses" element={<CampaignResponsesPage />} />
        <Route path="redemptions" element={<RedemptionsPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
