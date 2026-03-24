import { Routes, Route, Navigate } from "react-router-dom";
import AppLayout from "./components/common/AppLayout";
import Activity from "./pages/Activity";
import VendorDashboard from "./pages/VendorDashboard";
import CustomerDashboard from "./pages/CustomerDashboard";
import Manage from "./pages/Manage";

export default function App() {
  return (
    <AppLayout>
      <Routes>
        <Route path="/" element={<Navigate to="/customer" replace />} />
        <Route path="/customer" element={<CustomerDashboard />} />
        <Route path="/manage" element={<Manage />} />
        <Route path="/activity" element={<Activity />} />
        <Route path="/vendor" element={<VendorDashboard />} />
      </Routes>
    </AppLayout>
  );
}