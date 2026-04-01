import { useState } from "react";

import { API_BASE } from "../config/api";
import "../styles/Login.css";

function Login() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
let url = `${API_BASE}/auth/`;
      let body = { email, password };

      if (isLogin) {
        url += "login";
      } else {
        url += "register";
        body = { ...body, name, role: "CUSTOMER" };
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
if (!response.ok) {
  let errorMsg = "Invalid email or password";

  if (typeof data === "string") {
    errorMsg = data;
  } else if (data?.message) {
    errorMsg = data.message;
  } else if (response.status === 401) {
    errorMsg = "Invalid email or password";
  }

  throw new Error(errorMsg);
}

      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify(data.user));
      localStorage.setItem("role", data.user?.role);
      window.location.href = "/customer";

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
            <input
              type="text"
              placeholder="Full Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
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