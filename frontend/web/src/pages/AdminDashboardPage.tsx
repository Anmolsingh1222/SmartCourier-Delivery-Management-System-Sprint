export function AdminDashboardPage() {
  return (
    <section>
      <div className="page-head">
        <h2>Admin Intelligence Center</h2>
        <p>Monitor exception rates, hub configuration, and SLA performance signals.</p>
      </div>

      <div className="card-grid">
        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className="metric-value">Live</span>
          <span className="metric-label">Exception Queue</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className="metric-value">Hub</span>
          <span className="metric-label">Network Control</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className="metric-value">SLA</span>
          <span className="metric-label">Compliance Panel</span>
        </article>

        <article className="card" style={{ gridColumn: "span 12" }}>
          <p>
            Backend admin APIs are active and protected. Next UI iteration can add full table views for
            exceptions, users, hubs, and reports.
          </p>
        </article>
      </div>
    </section>
  );
}
