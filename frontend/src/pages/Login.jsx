import { useState } from "react";
import { API_BASE } from "../config/api";
import "../styles/Login.css";

function Login() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [role, setRole] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

 const handleSubmit = async (e) => {
  console.log("LOGIN CLICKED");
  e.preventDefault();
  setError("");
  setLoading(true);

  try {
    const endpoint = isLogin ? "/api/auth/login" : "/api/auth/register";

    const res = await fetch(`${API_BASE}${endpoint}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email,
        password,
        name,
        role,
      }),
    });

    if (!res.ok) {
      throw new Error("Invalid credentials");
    }

    const data = await res.json();

    // store real user
    localStorage.setItem("user", JSON.stringify(data));
    localStorage.setItem("role", data.role);

    console.log("Login success:", data);

    // redirect
    if (data.role === "VENDOR") {
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

              {/* 🔥 CLEAN ROLE UI */}
              <div className="role-section">
                <label className="role-title">Select Role</label>

                <div className="role-container">
                  <button
                    type="button"
                    onClick={() => setRole("CUSTOMER")}
                    className={`role-btn ${role === "CUSTOMER" ? "active" : ""}`}
                  >
                    Customer
                  </button>

                  <button
                    type="button"
                    onClick={() => setRole("VENDOR")}
                    className={`role-btn ${role === "VENDOR" ? "active" : ""}`}
                  >
                    Vendor
                  </button>
                </div>
              </div>
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