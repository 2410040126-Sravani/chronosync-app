export const API_BASE = "http://localhost:8082/api";

// // Helper to get auth headers
// export const getAuthHeaders = () => {
//   const token = localStorage.getItem('token');
//   return {
//     'Content-Type': 'application/json',
//     'Authorization': token ? `Bearer ${token}` : ''
//   };
// };

// // Generic fetch with auth
// export const authFetch = async (url, options = {}) => {
//   const headers = {
//     ...getAuthHeaders(),
//     ...options.headers
//   };
  
//   const response = await fetch(`${API_BASE}${url}`, {
//     ...options,
//     headers
//   });
  
//   if (response.status === 401) {
//     localStorage.removeItem('token');
//     localStorage.removeItem('user');
//     window.location.href = '/login';
//     throw new Error('Session expired. Please login again.');
//   }
  
//   return response;
// };