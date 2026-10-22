import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext.jsx';
import AppLayout from './components/AppLayout.jsx';
import ProtectedRoute from './routes/ProtectedRoute.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Crops from './pages/Crops.jsx';
import Demand from './pages/Demand.jsx';
import CultivationPlans from './pages/CultivationPlans.jsx';
import Recommendations from './pages/Recommendations.jsx';
import AdminFarmers from './pages/AdminFarmers.jsx';
import AdminCultivationPlans from './pages/AdminCultivationPlans.jsx';
import AdminAgriculturalStatistics from './pages/AdminAgriculturalStatistics.jsx';
import Profile from './pages/Profile.jsx';

function HomeRedirect() {
  const { user } = useAuth();
  return <Navigate to={user ? '/dashboard' : '/login'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/crops" element={<Crops />} />
          <Route path="/demand" element={<Demand />} />
          <Route element={<ProtectedRoute roles={['FARMER']} />}>
            <Route path="/profile" element={<Profile />} />
            <Route path="/plans" element={<CultivationPlans />} />
            <Route path="/recommendations" element={<Recommendations />} />
          </Route>
          <Route element={<ProtectedRoute roles={['ADMIN']} />}>
            <Route path="/admin/farmers" element={<AdminFarmers />} />
            <Route path="/admin/cultivation-plans" element={<AdminCultivationPlans />} />
            <Route path="/admin/statistics" element={<AdminAgriculturalStatistics />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  );
}
