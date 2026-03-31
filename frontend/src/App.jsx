import { Routes, Route, Navigate } from "react-router-dom";
import AppLayout from "./components/common/AppLayout";
import Activity from "./pages/Activity";
import VendorDashboard from "./pages/VendorDashboard";
import CustomerDashboard from "./pages/CustomerDashboard";
import Manage from "./pages/Manage";
import Login from "./pages/Login";

export default function App() {
  const token = localStorage.getItem("token");

  return (
    <Routes>
      {/* Login */}
      <Route path="/" element={<Login />} />
      <Route path="/login" element={<Login />} />

      {/* Protected */}
      <Route
        path="/customer"
        element={
          token ? (
            <AppLayout><CustomerDashboard /></AppLayout>
          ) : (
            <Navigate to="/login" />
          )
        }
      />

      <Route
        path="/manage"
        element={
          token ? (
            <AppLayout><Manage /></AppLayout>
          ) : (
            <Navigate to="/login" />
          )
        }
      />

      <Route
        path="/activity"
        element={
          token ? (
            <AppLayout><Activity /></AppLayout>
          ) : (
            <Navigate to="/login" />
          )
        }
      />

      <Route
        path="/vendor"
        element={
          token ? (
            <AppLayout><VendorDashboard /></AppLayout>
          ) : (
            <Navigate to="/login" />
          )
        }
      />
    </Routes>
  );
}