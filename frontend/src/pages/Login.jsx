import { useState } from "react";
import { API_BASE } from "../config/api";
import "../styles/Login.css";

function Login() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [role, setRole] = useState(""); // ✅ NEW
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      let url = `${API_BASE}/auth/`;
      let body = { email, password };

      // 🔹 LOGIN / REGISTER
      if (isLogin) {
        url += "login";
      } else {
        url += "register";

        if (!role) {
          throw new Error("Please select a role");
        }

        body = { ...body, name, role }; // ✅ use selected role
      }

      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
      });

      const text = await response.text();
      let data;

      try {
        data = JSON.parse(text);
        console.log("LOGIN RESPONSE:", data);
      } catch {
        data = text;
      }

      // 🔹 ERROR HANDLING
      if (!response.ok) {
        let errorMsg = "Invalid email or password";

        if (typeof data === "string") {
          errorMsg = data;
        } else if (data?.message) {
          errorMsg = data.message;
        }

        throw new Error(errorMsg);
      }

      if (!data.token) {
        throw new Error("Login failed");
      }

      // 🔹 STORE DATA
      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify(data));

      // ✅ USE BACKEND ROLE (NO GUESSING)
      const finalRole = data.role || role;

      localStorage.setItem("role", finalRole);

      // 🔹 REDIRECT
      if (finalRole === "VENDOR") {
        window.location.href = "/vendor";
      } else {
        window.location.href = "/customer";
      }

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">

        <div className="login-logo">CHRONOSYNC</div>
        <div className="login-tagline">Smart Milk Delivery System</div>

        <h2>{isLogin ? "Login" : "Register"}</h2>

        {error && <div className="login-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          {!isLogin && (
            <>
              <input
                type="text"
                placeholder="Full Name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />

              {/* ✅ ROLE DROPDOWN */}
              <select
                value={role}
                onChange={(e) => setRole(e.target.value)}
                required
              >
                <option value="">Select Role</option>
                <option value="CUSTOMER">Customer</option>
                <option value="VENDOR">Vendor</option>
              </select>
            </>
          )}

          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <button type="submit" disabled={loading}>
            {loading ? "Loading..." : isLogin ? "Login" : "Register"}
          </button>
        </form>

        <p className="login-toggle" onClick={() => setIsLogin(!isLogin)}>
          {isLogin
            ? "Don't have an account? Register"
            : "Already have an account? Login"}
        </p>
      </div>
    </div>
  );
}

export default Login;