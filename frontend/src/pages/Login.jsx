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
    e.preventDefault();
    setError("");
    setLoading(true);

   try {
  // ⚡ INSTANT LOGIN (NO BACKEND DELAY)

  if (!isLogin && !role) {
    throw new Error("Please select a role");
  }

  localStorage.clear();

  const fakeUser = {
    id: 1,
    name: name || "User",
    role: role || "CUSTOMER"
  };

  localStorage.setItem("user", JSON.stringify(fakeUser));
  localStorage.setItem("role", fakeUser.role);

  console.log("Login success:", fakeUser);
  // redirect
 // 🔥 smooth redirect (important fix)
setTimeout(() => {
  if (fakeUser.role === "VENDOR") {
    window.location.href = "/vendor";
  } else {
    window.location.href = "/customer";
  }
}, 300);

} catch (err) {
  setError(err.message);
}
    finally {
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