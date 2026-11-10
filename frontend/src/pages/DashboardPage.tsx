import { useMemo, type ReactNode } from "react";
import { Link } from "react-router-dom";
import { Building2, CircleDollarSign, DoorOpen, Wrench } from "lucide-react";
import { Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { usePortfolio } from "../context/PortfolioContext.tsx";
import { formatDate, formatMonth, formatPercent, formatZar, isLeaseActive, monthKey } from "../lib/format.ts";
import { buttonClass } from "../components/ui/Button.tsx";
import { StatusBadge } from "../components/ui/StatusBadge.tsx";
import { StatCard } from "../components/ui/StatCard.tsx";

const chartGrid = "var(--color-line)";
const chartInk = "var(--color-muted)";
const chartTeal = "var(--color-teal)";
const chartMist = "var(--color-mist)";
const statLinkClass =
  "block h-full rounded-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal focus-visible:ring-offset-2 focus-visible:ring-offset-paper";
const sectionLinkClass = "shrink-0 text-sm font-medium text-teal-deep underline-offset-2 hover:text-teal hover:underline";

function recentMonths(count = 6) {
  const cursor = new Date();
  cursor.setDate(1);
  return Array.from({ length: count }, (_, index) => {
    const date = new Date(cursor.getFullYear(), cursor.getMonth() - (count - 1 - index), 1);
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
    const label = new Intl.DateTimeFormat("en-ZA", { month: "short" }).format(date);
    return { key, label };
  });
}

function formatAxisRand(value: number) {
  const amount = Number(value);
  if (!Number.isFinite(amount)) return "";
  if (Math.abs(amount) >= 1000) {
    const thousands = amount / 1000;
    const digits = Number.isInteger(thousands) ? 0 : 1;
    return `R${thousands.toFixed(digits)}k`;
  }
  return `R${Math.round(amount)}`;
}

function QuietEmpty({ title, body, action }: { title: string; body: string; action?: ReactNode }) {
  return (
    <div className="flex min-h-48 flex-col items-center justify-center px-2 py-8 text-center">
      <p className="font-display text-xl">{title}</p>
      <p className="mt-2 max-w-sm text-sm leading-6 text-muted">{body}</p>
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}

export function DashboardPage() {
  const { properties, leases, payments, maintenance, managers, agents, source } = usePortfolio();

  const occupied = properties.filter((property) => !property.available).length;
  const available = Math.max(properties.length - occupied, 0);
  const occupancy = properties.length === 0 ? 0 : occupied / properties.length;
  const currentMonth = monthKey();
  const collected = payments
    .filter((payment) => payment.status === "PAID" && payment.paymentDate.startsWith(currentMonth))
    .reduce((sum, payment) => sum + payment.amount, 0);
  const pendingTickets = maintenance.filter((item) => item.status === "OPEN" || item.status === "IN_PROGRESS");
  const activeLeases = leases.filter((lease) => isLeaseActive(lease)).length;

  const revenue = useMemo(() => {
    return recentMonths().map((month) => ({
      label: month.label,
      collected: payments
        .filter((payment) => payment.status === "PAID" && payment.paymentDate.startsWith(month.key))
        .reduce((sum, payment) => sum + payment.amount, 0),
    }));
  }, [payments]);

  const slices = [
    { name: "Occupied", value: occupied, fill: chartTeal },
    { name: "Available", value: available, fill: chartMist },
  ].filter((slice) => slice.value > 0);

  const recentPayments = [...payments].sort((a, b) => b.paymentDate.localeCompare(a.paymentDate)).slice(0, 5);
  const openWork = [...pendingTickets].sort((a, b) => b.reportedDate.localeCompare(a.reportedDate)).slice(0, 4);

  return (
    <div className="space-y-6">
      <p className="max-w-2xl text-sm leading-6 text-muted">
        Occupancy, rent collected in {formatMonth()}, and maintenance still open. {activeLeases}{" "}
        {activeLeases === 1 ? "lease" : "leases"} currently running.
      </p>

      {source === "api" && properties.length === 0 ? (
        <p className="rounded-xl border border-line bg-card px-4 py-3 text-sm leading-6 text-muted">
          The service responded, and this organisation has no properties yet. Add the first address to start the portfolio.
        </p>
      ) : null}

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Link to="/properties" className={statLinkClass}>
          <StatCard
            label="Active properties"
            value={String(properties.length)}
            detail={`${occupied} occupied · ${available} available`}
            icon={<Building2 size={18} />}
            iconClass="bg-teal-soft text-teal-deep"
          />
        </Link>
        <Link to="/leases" className={statLinkClass}>
          <StatCard
            label="Occupancy rate"
            value={formatPercent(occupancy)}
            detail={properties.length === 0 ? "No properties yet" : `${occupied} of ${properties.length} let`}
            icon={<DoorOpen size={18} />}
            iconClass="bg-navy-soft text-navy"
          />
        </Link>
        <Link to="/payments" className={statLinkClass}>
          <StatCard
            label="Monthly revenue collected"
            value={formatZar(collected)}
            detail={`Paid in ${formatMonth()}`}
            icon={<CircleDollarSign size={18} />}
            iconClass="bg-teal-soft text-teal-deep"
          />
        </Link>
        <Link to="/maintenance" className={statLinkClass}>
          <StatCard
            label="Pending maintenance"
            value={String(pendingTickets.length)}
            detail="Open and in progress"
            icon={<Wrench size={18} />}
            iconClass="bg-clay-soft text-clay"
          />
        </Link>
      </section>

      <section className="grid gap-4 xl:grid-cols-5">
        <article className="panel p-5 xl:col-span-3">
          <h2 className="font-display text-2xl">Rent collected</h2>
          <p className="mt-1 text-sm text-muted">Paid payments across the last six months.</p>
          {payments.length === 0 ? (
            <QuietEmpty
              title="No payment history yet"
              body="Record a payment against a lease. This chart uses only payments that have actually been saved."
              action={
                <Link to="/payments" className={buttonClass({ size: "sm" })}>
                  Go to payments
                </Link>
              }
            />
          ) : (
            <div className="mt-4 h-64 min-w-0">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={revenue} barSize={22} margin={{ top: 8, right: 4, left: 0, bottom: 0 }}>
                  <CartesianGrid vertical={false} stroke={chartGrid} />
                  <XAxis
                    dataKey="label"
                    tickLine={false}
                    axisLine={false}
                    interval={0}
                    tick={{ fill: chartInk, fontSize: 12 }}
                  />
                  <YAxis
                    tickLine={false}
                    axisLine={false}
                    width={56}
                    tick={{ fill: chartInk, fontSize: 12 }}
                    tickFormatter={formatAxisRand}
                  />
                  <Tooltip
                    cursor={{ fill: "rgb(18 36 59 / 0.04)" }}
                    formatter={(value) => formatZar(Number(value ?? 0))}
                    contentStyle={{
                      borderRadius: 8,
                      borderColor: "var(--color-line)",
                      background: "var(--color-porcelain)",
                      color: "var(--color-ink)",
                      fontSize: 13,
                    }}
                  />
                  <Bar dataKey="collected" name="Collected" fill={chartTeal} radius={[4, 4, 0, 0]} maxBarSize={36} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </article>

        <article className="panel flex flex-col p-5 xl:col-span-2">
          <h2 className="font-display text-2xl">Occupancy</h2>
          <p className="mt-1 text-sm text-muted">Available units against those already let.</p>
          {properties.length === 0 ? (
            <QuietEmpty
              title="No properties on the books"
              body="Add a property and mark it available or occupied. The split appears once a property exists."
              action={
                <Link to="/properties" className={buttonClass({ size: "sm" })}>
                  Add a property
                </Link>
              }
            />
          ) : (
            <>
              <div className="relative mt-2 h-52">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={slices} dataKey="value" nameKey="name" innerRadius={58} outerRadius={78} paddingAngle={slices.length > 1 ? 3 : 0} stroke="none">
                      {slices.map((slice) => (
                        <Cell key={slice.name} fill={slice.fill} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value) => `${Number(value ?? 0)} properties`} />
                  </PieChart>
                </ResponsiveContainer>
                <div className="pointer-events-none absolute inset-0 grid place-items-center">
                  <div className="text-center">
                    <p className="num font-display text-3xl">{formatPercent(occupancy)}</p>
                    <p className="text-xs text-muted">occupied</p>
                  </div>
                </div>
              </div>
              <ul className="mt-2 flex flex-wrap justify-center gap-x-4 gap-y-2 text-sm text-muted">
                <li className="flex items-center gap-2">
                  <span className="h-2.5 w-2.5 rounded-full bg-teal" /> Occupied
                  <span className="num text-ink">{occupied}</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="h-2.5 w-2.5 rounded-full bg-mist" /> Available
                  <span className="num text-ink">{available}</span>
                </li>
              </ul>
            </>
          )}
        </article>
      </section>

      <section className="grid gap-4 lg:grid-cols-2">
        <article className="panel p-5">
          <div className="flex items-center justify-between gap-3">
            <h2 className="font-display text-2xl">Latest payments</h2>
            <Link to="/payments" className={sectionLinkClass}>
              View all
            </Link>
          </div>
          {recentPayments.length === 0 ? (
            <QuietEmpty
              title="No payments recorded"
              body="Payments appear here after they are recorded against a lease."
              action={
                <Link to="/payments" className={sectionLinkClass}>
                  Record a payment
                </Link>
              }
            />
          ) : (
            <ul className="mt-4 divide-y divide-line">
              {recentPayments.map((payment) => {
                const lease = leases.find((item) => item.leaseId === payment.leaseId);
                return (
                  <li key={payment.paymentId} className="flex flex-col gap-2 py-3 sm:flex-row sm:items-center sm:justify-between">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium">{lease?.tenantName ?? payment.leaseId}</p>
                      <p className="text-xs text-muted">
                        {formatDate(payment.paymentDate)} · {payment.paymentMethod}
                      </p>
                    </div>
                    <div className="flex items-center justify-between gap-3 sm:justify-end">
                      <span className="num text-sm font-medium">{formatZar(payment.amount)}</span>
                      <StatusBadge status={payment.status} />
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </article>

        <article className="panel p-5">
          <div className="flex items-center justify-between gap-3">
            <h2 className="font-display text-2xl">Open maintenance</h2>
            <Link to="/maintenance" className={sectionLinkClass}>
              Open board
            </Link>
          </div>
          {openWork.length === 0 ? (
            <QuietEmpty
              title="No open tickets"
              body="Open and in-progress requests show here. Resolved work stays on the maintenance board."
              action={
                <Link to="/maintenance" className={sectionLinkClass}>
                  Log a request
                </Link>
              }
            />
          ) : (
            <ul className="mt-4 space-y-3">
              {openWork.map((ticket) => (
                <li key={ticket.requestId} className="rounded-lg border border-line px-3 py-3">
                  <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                    <p className="text-sm leading-6">{ticket.description}</p>
                    <StatusBadge status={ticket.status} />
                  </div>
                  <p className="mt-2 text-xs text-muted">
                    {formatDate(ticket.reportedDate)} · {formatZar(ticket.cost)}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </article>
      </section>

      {managers.length > 0 || agents.length > 0 ? (
        <section className="grid gap-4 md:grid-cols-2">
          <article className="panel p-5">
            <h2 className="font-display text-2xl">Managers</h2>
            <ul className="mt-4 space-y-3">
              {managers.map((manager) => (
                <li key={manager.managerId} className="flex flex-col gap-1 text-sm sm:flex-row sm:items-center sm:justify-between sm:gap-3">
                  <div className="min-w-0">
                    <p className="font-medium">{manager.name}</p>
                    <p className="text-xs text-muted">{manager.employeeId}</p>
                  </div>
                  <p className="text-muted sm:text-right">{manager.contactNumber}</p>
                </li>
              ))}
            </ul>
          </article>
          <article className="panel p-5">
            <h2 className="font-display text-2xl">Agents</h2>
            <ul className="mt-4 space-y-3">
              {agents.map((agent) => (
                <li key={agent.agentId} className="flex flex-col gap-1 text-sm sm:flex-row sm:items-center sm:justify-between sm:gap-3">
                  <div className="min-w-0">
                    <p className="truncate font-medium">{agent.name}</p>
                    <p className="truncate text-xs text-muted">{agent.email}</p>
                  </div>
                  <p className="text-muted sm:shrink-0 sm:text-right">{agent.role}</p>
                </li>
              ))}
            </ul>
          </article>
        </section>
      ) : null}
    </div>
  );
}
