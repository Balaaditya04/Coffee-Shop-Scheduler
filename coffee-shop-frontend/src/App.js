import React, { useState, useEffect, useCallback } from 'react';

const API_BASE = 'http://localhost:8080/api';

// ═══════════════════════════════════════════════════════════════════════════
// OAUTH CALLBACK HANDLER
// ═══════════════════════════════════════════════════════════════════════════
function OAuthCallback({ onSuccess }) {
    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');
        const username = params.get('username');

        if (token && username) {
            localStorage.setItem('token', token);
            localStorage.setItem('username', username);
            onSuccess({ token, username });
            // Clear URL params
            window.history.replaceState({}, document.title, '/');
        }
    }, [onSuccess]);

    return (
        <div className="auth-page">
            <div className="auth-container">
                <h1>Coffee<span>Shop</span></h1>
                <h2>Signing you in...</h2>
            </div>
        </div>
    );
}

// ═══════════════════════════════════════════════════════════════════════════
// LOGIN PAGE COMPONENT
// ═══════════════════════════════════════════════════════════════════════════
function LoginPage({ onLogin, onSwitchToSignup }) {
    const [usernameOrEmail, setUsernameOrEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const res = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ usernameOrEmail, password })
            });

            const data = await res.json();

            if (data.success) {
                localStorage.setItem('token', data.token);
                localStorage.setItem('username', data.username);
                onLogin(data);
            } else {
                setError(data.message || 'Login failed');
            }
        } catch (err) {
            setError('Connection error. Please check if backend is running.');
        }
        setLoading(false);
    };

    const handleGoogleLogin = () => {
        window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
                <h1>Coffee<span>Shop</span></h1>
                <h2>Welcome Back</h2>

                {error && <div className="auth-error">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Username or Email</label>
                        <input
                            type="text"
                            value={usernameOrEmail}
                            onChange={(e) => setUsernameOrEmail(e.target.value)}
                            placeholder="Enter username or email"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Enter password"
                            required
                        />
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading}>
                        {loading ? 'Logging in...' : 'Login'}
                    </button>
                </form>

                <div className="divider">
                    <span>or</span>
                </div>

                <button onClick={handleGoogleLogin} className="btn btn-google">
                    <svg viewBox="0 0 24 24" width="18" height="18" style={{ marginRight: '10px' }}>
                        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                    </svg>
                    Continue with Google
                </button>

                <p className="auth-switch">
                    Don't have an account?{' '}
                    <button onClick={onSwitchToSignup} className="link-btn">Sign up</button>
                </p>
            </div>
        </div>
    );
}

// ═══════════════════════════════════════════════════════════════════════════
// SIGNUP PAGE COMPONENT
// ═══════════════════════════════════════════════════════════════════════════
function SignupPage({ onSignup, onSwitchToLogin }) {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        if (password !== confirmPassword) {
            setError('Passwords do not match');
            setLoading(false);
            return;
        }

        if (password.length < 6) {
            setError('Password must be at least 6 characters');
            setLoading(false);
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/auth/signup`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password })
            });

            const data = await res.json();

            if (data.success) {
                localStorage.setItem('token', data.token);
                localStorage.setItem('username', data.username);
                onSignup(data);
            } else {
                setError(data.message || 'Signup failed');
            }
        } catch (err) {
            setError('Connection error. Please check if backend is running.');
        }
        setLoading(false);
    };

    const handleGoogleLogin = () => {
        window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
                <h1>Coffee<span>Shop</span></h1>
                <h2>Create Account</h2>

                {error && <div className="auth-error">{error}</div>}

                <button onClick={handleGoogleLogin} className="btn btn-google" style={{ marginBottom: '20px' }}>
                    <svg viewBox="0 0 24 24" width="18" height="18" style={{ marginRight: '10px' }}>
                        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                    </svg>
                    Continue with Google
                </button>

                <div className="divider">
                    <span>or</span>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Username</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="Choose a username"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="Enter your email"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Create a password (min 6 chars)"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Confirm Password</label>
                        <input
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            placeholder="Confirm your password"
                            required
                        />
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading}>
                        {loading ? 'Creating Account...' : 'Sign Up'}
                    </button>
                </form>

                <p className="auth-switch">
                    Already have an account?{' '}
                    <button onClick={onSwitchToLogin} className="link-btn">Login</button>
                </p>
            </div>
        </div>
    );
}

// ═══════════════════════════════════════════════════════════════════════════
// DASHBOARD COMPONENT (Main scheduler view)
// ═══════════════════════════════════════════════════════════════════════════
function Dashboard({ user, onLogout, onNavigateToStats }) {
    const [queue, setQueue] = useState([]);
    const [baristas, setBaristas] = useState([]);
    const [stats, setStats] = useState({});
    const [alerts, setAlerts] = useState([]);

    const [drinkName, setDrinkName] = useState('');
    const [prepTime, setPrepTime] = useState(3);
    const [loyaltyTier, setLoyaltyTier] = useState(1);
    const [isRegular, setIsRegular] = useState(false);

    const fetchData = useCallback(async () => {
        try {
            const [queueRes, baristasRes, statsRes, alertsRes] = await Promise.all([
                fetch(`${API_BASE}/orders?username=${encodeURIComponent(user.username)}`),
                fetch(`${API_BASE}/baristas`),
                fetch(`${API_BASE}/stats?username=${encodeURIComponent(user.username)}`),
                fetch(`${API_BASE}/alerts`)
            ]);

            setQueue(await queueRes.json());
            setBaristas(await baristasRes.json());
            setStats(await statsRes.json());
            setAlerts(await alertsRes.json());
        } catch (error) {
            console.error('Failed to fetch data:', error);
        }
    }, [user.username]);

    useEffect(() => {
        fetchData();
        const interval = setInterval(fetchData, 2000);
        return () => clearInterval(interval);
    }, [fetchData]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!drinkName.trim()) return;

        try {
            await fetch(`${API_BASE}/orders`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    drinkName,
                    prepTimeMinutes: prepTime,
                    loyaltyTier,
                    isRegularCustomer: isRegular,
                    username: user.username
                })
            });
            setDrinkName('');
            fetchData();
        } catch (error) {
            console.error('Failed to create order:', error);
        }
    };

    const handleComplete = async (baristaId) => {
        try {
            await fetch(`${API_BASE}/baristas/${baristaId}/complete`, { method: 'POST' });
            fetchData();
        } catch (error) {
            console.error('Failed to complete order:', error);
        }
    };

    const getOrderClass = (order) => {
        const waitMinutes = (Date.now() - new Date(order.arrivalTime).getTime()) / 60000;
        if (waitMinutes >= 8) return 'urgent';
        if (waitMinutes >= 6) return 'warning';
        return '';
    };

    const formatWait = (arrivalTime) => {
        const minutes = Math.floor((Date.now() - new Date(arrivalTime).getTime()) / 60000);
        const seconds = Math.floor(((Date.now() - new Date(arrivalTime).getTime()) % 60000) / 1000);
        return `${minutes}m ${seconds}s`;
    };

    return (
        <div className="app">
            <nav className="navbar">
                <div className="logo">
                    <h1>Coffee<span>Shop</span></h1>
                </div>
                <div className="user-info">
                    <span className="welcome-text">Welcome, <strong>{user.username}</strong></span>
                    <button onClick={onNavigateToStats} className="btn btn-stats">Analytics</button>
                    <button onClick={onLogout} className="btn btn-logout">Logout</button>
                </div>
            </nav>

            <header className="header">
                <h1>Brewing Excellence</h1>
                <p>Advanced Priority Queue Management</p>
                <div style={{ fontSize: '0.85rem', color: '#5D4037', marginTop: '12px', fontWeight: 500 }}>
                    Formula: (0.40 × Wait) + (0.25 × Complexity) + (0.10 × Loyalty) + (0.25 × Urgency)
                </div>
            </header>

            {/* Stats Bar */}
            <div className="stats-bar">
                <div className="stat-card">
                    <div className="value">{stats.queueSize || 0}</div>
                    <div className="label">In Queue</div>
                </div>
                <div className="stat-card">
                    <div className="value">{stats.completedCount || 0}</div>
                    <div className="label">Completed</div>
                </div>
                <div className="stat-card">
                    <div className="value">{stats.averageWaitMinutes || 0}m</div>
                    <div className="label">Avg Wait</div>
                </div>
                <div className="stat-card">
                    <div className="value">{stats.timeoutCount || 0}</div>
                    <div className="label">Timeouts</div>
                </div>
            </div>

            {/* Main Grid */}
            <div className="main-grid">
                {/* Left: New Order Form */}
                <div className="section">
                    <h2><span className="icon">📝</span> New Order</h2>
                    <form className="order-form" onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label>Drink Name</label>
                            <input
                                type="text"
                                value={drinkName}
                                onChange={(e) => setDrinkName(e.target.value)}
                                placeholder="e.g., Caramel Latte"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label>Prep Time (Complexity)</label>
                            <select value={prepTime} onChange={(e) => setPrepTime(Number(e.target.value))}>
                                <option value={2}>2 min - Simple (Espresso)</option>
                                <option value={3}>3 min - Easy (Americano)</option>
                                <option value={4}>4 min - Medium (Latte)</option>
                                <option value={5}>5 min - Complex (Mocha)</option>
                                <option value={6}>6 min - Elaborate (Frappuccino)</option>
                                <option value={8}>8 min - Very Complex (Special)</option>
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Loyalty Tier</label>
                            <select value={loyaltyTier} onChange={(e) => setLoyaltyTier(Number(e.target.value))}>
                                <option value={1}>Tier 1 - New Customer</option>
                                <option value={2}>Tier 2 - Bronze</option>
                                <option value={3}>Tier 3 - Silver</option>
                                <option value={4}>Tier 4 - Gold</option>
                                <option value={5}>Tier 5 - VIP</option>
                            </select>
                        </div>

                        <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <input
                                type="checkbox"
                                id="isRegular"
                                checked={isRegular}
                                onChange={(e) => setIsRegular(e.target.checked)}
                                style={{ width: 'auto' }}
                            />
                            <label htmlFor="isRegular" style={{ margin: 0 }}>Regular Customer (+50 loyalty)</label>
                        </div>

                        <button type="submit" className="btn btn-primary">Add Order</button>
                    </form>

                    {alerts.length > 0 && (
                        <div style={{ marginTop: '25px' }}>
                            <h3 style={{ marginBottom: '10px', fontSize: '1rem' }}>⚠️ Alerts</h3>
                            <div className="alerts-list">
                                {alerts.slice(-5).reverse().map((alert, idx) => (
                                    <div
                                        key={idx}
                                        className={`alert-item ${alert.includes('CRITICAL') ? 'critical' : alert.includes('FAIRNESS') ? 'fairness' : 'warning'}`}
                                    >
                                        {alert}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                {/* Center: Order Queue */}
                <div className="section">
                    <h2><span className="icon">📋</span> Order Queue ({queue.length})</h2>
                    {queue.length === 0 ? (
                        <div className="empty-state">
                            <div className="icon">☕</div>
                            <p>No orders in queue</p>
                        </div>
                    ) : (
                        <div className="queue-list">
                            {queue.map((order) => (
                                <div key={order.id} className={`order-card ${getOrderClass(order)}`}>
                                    <div className="order-info">
                                        <h4>#{order.id} - {order.drinkName}</h4>
                                        <div className="meta">
                                            Wait: {formatWait(order.arrivalTime)} |
                                            Prep: {order.prepTimeMinutes}m |
                                            Loyalty: T{order.loyaltyTier}{order.regularCustomer ? '★' : ''} |
                                            Skips: {order.skipCount}
                                        </div>
                                        {order.priorityExplanation && (
                                            <div className="priority-explanation">{order.priorityExplanation}</div>
                                        )}
                                    </div>
                                    <div className="order-priority">{Math.round(order.priority)}</div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Right: Baristas */}
                <div className="section">
                    <h2><span className="icon">👨‍🍳</span> Baristas</h2>
                    <div className="barista-list">
                        {baristas.map((barista) => {
                            const currentOrder = barista.assignedOrders?.find(o => o.status === 'IN_PROGRESS');
                            const workloadPercent = Math.min(100, (barista.totalPendingMinutes || 0) * 10);
                            const workloadRatio = stats.baristaWorkloads?.[barista.name]?.ratio || 1;

                            return (
                                <div key={barista.id} className="barista-card">
                                    <div className="name">
                                        {barista.name}
                                        <span className={`status ${barista.available ? 'available' : 'busy'}`}>
                                            {barista.available ? 'Available' : 'Busy'}
                                        </span>
                                    </div>

                                    <div className="workload">
                                        <span style={{ fontSize: '0.85rem', color: '#a0a0c0' }}>
                                            Workload: {barista.totalPendingMinutes || 0} min
                                            <span style={{ color: workloadRatio > 1.2 ? '#ff6b6b' : workloadRatio < 0.8 ? '#4ecdc4' : '#a0a0c0', marginLeft: '5px' }}>
                                                ({workloadRatio}x)
                                            </span>
                                        </span>
                                        <div className="workload-bar">
                                            <div
                                                className={`workload-fill ${workloadRatio > 1.2 ? 'high' : ''}`}
                                                style={{ width: `${workloadPercent}%` }}
                                            />
                                        </div>
                                    </div>

                                    {currentOrder && (
                                        <div className="current-order">
                                            <div className="label">Making:</div>
                                            <strong>#{currentOrder.id} - {currentOrder.drinkName}</strong>
                                        </div>
                                    )}

                                    {!barista.available && (
                                        <button
                                            className="btn btn-success"
                                            style={{ marginTop: '10px', width: '100%' }}
                                            onClick={() => handleComplete(barista.id)}
                                        >
                                            Complete Order
                                        </button>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>
        </div>
    );
}

function StatisticsPage({ user, onBack }) {
    const [baristaStats, setBaristaStats] = useState([]);
    const [overallStats, setOverallStats] = useState({});
    const [simulationResults, setSimulationResults] = useState([]);
    const [runningSimulation, setRunningSimulation] = useState(false);
    const [complaints, setComplaints] = useState([]);
    const [complaintBarista, setComplaintBarista] = useState('Alice');
    const [complaintMessage, setComplaintMessage] = useState('');
    const [submittingComplaint, setSubmittingComplaint] = useState(false);

    useEffect(() => {
        fetchStats();
        fetchComplaints();
    }, []);

    const fetchStats = async () => {
        try {
            const [statsRes, baristaRes] = await Promise.all([
                fetch(`${API_BASE}/stats`),
                fetch(`${API_BASE}/stats/baristas`)
            ]);
            setOverallStats(await statsRes.json());
            setBaristaStats(await baristaRes.json());
        } catch (error) {
            console.error('Failed to fetch stats:', error);
        }
    };

    const fetchComplaints = async () => {
        try {
            const res = await fetch(`${API_BASE}/complaints`);
            setComplaints(await res.json());
        } catch (error) {
            console.error('Failed to fetch complaints:', error);
        }
    };

    const runSimulation = async () => {
        setRunningSimulation(true);
        try {
            const res = await fetch(`${API_BASE}/simulation/run?testCases=10`, { method: 'POST' });
            setSimulationResults(await res.json());
        } catch (error) {
            console.error('Simulation failed:', error);
        } finally {
            setRunningSimulation(false);
        }
    };

    const submitComplaint = async (e) => {
        e.preventDefault();
        if (!complaintMessage.trim()) return;

        setSubmittingComplaint(true);
        try {
            await fetch(`${API_BASE}/complaints`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    baristaName: complaintBarista,
                    username: user.username,
                    message: complaintMessage
                })
            });
            setComplaintMessage('');
            fetchComplaints();
        } catch (error) {
            console.error('Failed to submit complaint:', error);
        } finally {
            setSubmittingComplaint(false);
        }
    };

    const formatDate = (timestamp) => {
        return new Date(timestamp).toLocaleString();
    };

    return (
        <div className="app">
            <nav className="navbar">
                <div className="logo">
                    <h1>Coffee<span>Shop</span></h1>
                </div>
                <div className="user-info">
                    <button onClick={onBack} className="btn btn-logout">← Back to Dashboard</button>
                </div>
            </nav>

            <header className="header">
                <h1>Insights & Analytics</h1>
                <p>Performance tracking and quality control</p>
            </header>

            <div className="stats-bar">
                <div className="stat-card">
                    <span className="value">{overallStats.completedCount || 0}</span>
                    <span className="label">Orders Processed</span>
                </div>
                <div className="stat-card">
                    <span className="value">{overallStats.averageWaitMinutes || 0}m</span>
                    <span className="label">Avg Wait Time</span>
                </div>
                <div className="stat-card">
                    <span className="value">{overallStats.queueSize || 0}</span>
                    <span className="label">Current Queue</span>
                </div>
                <div className="stat-card">
                    <span className="value">{overallStats.timeoutCount || 0}</span>
                    <span className="label">Service Timeouts</span>
                </div>
            </div>

            <div className="main-grid">
                <div className="section" style={{ gridColumn: 'span 2' }}>
                    <h2>Performance Metrics</h2>
                    <table className="stats-table">
                        <thead>
                            <tr>
                                <th>Barista</th>
                                <th>Completed</th>
                                <th>Avg Time</th>
                                <th>Timeouts</th>
                                <th>Workload</th>
                            </tr>
                        </thead>
                        <tbody>
                            {baristaStats.map((b) => (
                                <tr key={b.id}>
                                    <td><strong>{b.name}</strong></td>
                                    <td>{b.ordersCompleted}</td>
                                    <td>{b.avgTimePerOrder}m</td>
                                    <td>{b.timeouts}</td>
                                    <td>{b.workloadRatio}x</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div className="section" style={{ gridColumn: 'span 3' }}>
                    <h2>Stress Test Simulation</h2>
                    <button
                        className="btn btn-primary btn-stats"
                        onClick={runSimulation}
                        disabled={runningSimulation}
                        style={{ marginBottom: '24px' }}
                    >
                        {runningSimulation ? 'Processing...' : 'Run 10 Case Simulation'}
                    </button>

                    {simulationResults.length > 0 && (
                        <table className="stats-table">
                            <thead>
                                <tr>
                                    <th>TestCase</th>
                                    <th>Total Orders</th>
                                    <th>Avg Wait</th>
                                    <th>A|B|C Distribution</th>
                                    <th>Timeouts</th>
                                </tr>
                            </thead>
                            <tbody>
                                {simulationResults.map((r) => (
                                    <tr key={r.testCase}>
                                        <td>Case #{r.testCase}</td>
                                        <td>{r.totalOrders}</td>
                                        <td>{r.avgWaitTime}m</td>
                                        <td>{r.b1Orders} | {r.b2Orders} | {r.b3Orders}</td>
                                        <td style={{ color: r.timeouts > 0 ? '#A52A2A' : '#6D8B74', fontWeight: 600 }}>{r.timeouts}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>

                <div className="section" style={{ gridColumn: 'span 3' }}>
                    <h2>Quality Control (Complaints)</h2>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px' }}>
                        <form onSubmit={submitComplaint} className="complaint-form">
                            <div className="form-group">
                                <label>Assign Responsibility</label>
                                <select value={complaintBarista} onChange={(e) => setComplaintBarista(e.target.value)}>
                                    <option value="Alice">Alice</option>
                                    <option value="Bob">Bob</option>
                                    <option value="Charlie">Charlie</option>
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Detailed Feedback</label>
                                <textarea
                                    value={complaintMessage}
                                    onChange={(e) => setComplaintMessage(e.target.value)}
                                    placeholder="Please describe the issue..."
                                    rows={4}
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-primary" disabled={submittingComplaint}>
                                {submittingComplaint ? 'Submitting...' : 'Log Feedback'}
                            </button>
                        </form>

                        <div className="complaints-list">
                            <h3>Registry</h3>
                            {complaints.length === 0 ? <p>No feedback logged</p> :
                                complaints.slice(0, 10).map((c) => (
                                    <div key={c.id} className="complaint-item">
                                        <div className="complaint-header">
                                            <strong>{c.baristaName}</strong>
                                            <span className="complaint-date">{formatDate(c.createdAt)}</span>
                                        </div>
                                        <div className="complaint-message">{c.message}</div>
                                        <div className="complaint-user">— {c.username}</div>
                                    </div>
                                ))
                            }
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

// ═══════════════════════════════════════════════════════════════════════════
// MAIN APP - Handles auth state + OAuth callback + Page Navigation
// ═══════════════════════════════════════════════════════════════════════════
function App() {
    const [user, setUser] = useState(null);
    const [page, setPage] = useState('login');
    const [currentView, setCurrentView] = useState('dashboard');

    useEffect(() => {
        // Check for OAuth callback
        if (window.location.pathname === '/oauth-callback' || window.location.search.includes('token=')) {
            return; // Let OAuthCallback handle it
        }

        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');
        if (token && username) {
            setUser({ username, token });
        }
    }, []);

    const handleLogin = (data) => {
        setUser({ username: data.username, token: data.token });
    };

    const handleSignup = (data) => {
        setUser({ username: data.username, token: data.token });
    };

    const handleOAuthSuccess = (data) => {
        setUser({ username: data.username, token: data.token });
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        setUser(null);
        setPage('login');
        setCurrentView('dashboard');
    };

    // Handle OAuth callback
    if (window.location.search.includes('token=') || window.location.pathname === '/oauth-callback') {
        return <OAuthCallback onSuccess={handleOAuthSuccess} />;
    }

    if (user) {
        if (currentView === 'statistics') {
            return <StatisticsPage user={user} onBack={() => setCurrentView('dashboard')} />;
        }
        return <Dashboard user={user} onLogout={handleLogout} onNavigateToStats={() => setCurrentView('statistics')} />;
    }

    if (page === 'signup') {
        return <SignupPage onSignup={handleSignup} onSwitchToLogin={() => setPage('login')} />;
    }

    return <LoginPage onLogin={handleLogin} onSwitchToSignup={() => setPage('signup')} />;
}

export default App;

