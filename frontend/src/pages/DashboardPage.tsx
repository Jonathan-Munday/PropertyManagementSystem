import { useMemo } from "react";
import { Link } from "react-router-dom";
import { Building2, CircleDollarSign, DoorOpen, Wrench } from "lucide-react";
import { Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { usePortfolio } from "../context/PortfolioContext.tsx";
import { formatDate, formatMonth, formatPercent, formatZar, isLeaseActive, monthKey } from "../lib/format.ts";
import { StatusBadge } from "../components/ui/StatusBadge.tsx";
import { StatCard } from "../components/ui/StatCard.tsx";

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

export function DashboardPage() {
  const { properties, leases, payments, maintenance, managers, agents, source } = usePortfolio();

  const occupied = properties.filter((property) => !property.available).length;
  const occupancy = properties.length === 0 ? 0 : occupied / properties.length;
  const currentMonth = monthKey();
  const collected = payments
    .filter((payment) => payment.status === "PAID" && payment.paymentDate.startsWith(currentMonth))
    .reduce((sum, payment) => sum + payment.amount, 0);
  const pendingTickets = maintenance.filter((item) => item.status === "OPEN" || item.status === "IN_PROGRESS");
  const activeLeases = leases.filter((lease) => isLeaseActive(lease.endDate)).length;

  const revenue = useMemo(() => {
    return recentMonths().map((month) => ({
      label: month.label,
      collected: payments
        .filter((payment) => payment.status === "PAID" && payment.paymentDate.startsWith(month.key))
        .reduce((sum, payment) => sum + payment.amount, 0),
    }));
  }, [payments]);

  const slices = [
    { name: "Occupied", value: occupied, fill: "#1e5c40" },
    { name: "Available", value: Math.max(properties.length - occupied, 0), fill: "#b8884a" },
  ];

  const recentPayments = [...payments].sort((a, b) => b.paymentDate.localeCompare(a.paymentDate)).slice(0, 5);
  const openWork = [...pendingTickets].sort((a, b) => b.reportedDate.localeCompare(a.reportedDate)).slice(0, 4);

  return (
    <div className="space-y-6">
      <p className="max-w-2xl text-sm leading-6 text-muted">
        Occupancy, rent collected in {formatMonth()}, and maintenance still open. {activeLeases}{" "}
        {activeLeases === 1 ? "lease" : "leases"} currently running.
      </p>

      {source === "api" && properties.length === 0 ? (
        <p className="rounded-xl border border-line bg-card px-4 py-3 text-sm text-muted">
          The API responded, and the property list is empty. Add a property to start the portfolio.
        </p>
      ) : null}

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Link to="/properties" className="rounded-2xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brass">
          <StatCard
            label="Active properties"
            value={String(properties.length)}
            detail={`${occupied} occupied · ${properties.length - occupied} available`}
            icon={<Building2 size={18} />}
            iconClass="bg-sea-soft text-sea"
          />
        </Link>
        <Link to="/leases" className="rounded-2xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brass">
          <StatCard
            label="Occupancy rate"
            value={formatPercent(occupancy)}
            detail={properties.length === 0 ? "No properties yet" : `${occupied} of ${properties.length} let`}
            icon={<DoorOpen size={18} />}
            iconClass="bg-moss-soft text-moss"
          />
        </Link>
        <Link to="/payments" className="rounded-2xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brass">
          <StatCard
            label="Monthly revenue collected"
            value={formatZar(collected)}
            detail={`Paid in ${formatMonth()}`}
            icon={<CircleDollarSign size={18} />}
            iconClass="bg-brass-light text-brass-deep"
          />
        </Link>
        <Link to="/maintenance" className="rounded-2xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brass">
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
        <article className="rounded-2xl border border-line bg-card p-5 xl:col-span-3">
          <h2 className="font-display text-2xl">Rent collected</h2>
          <p className="mt-1 text-sm text-muted">Paid payments across the last six months.</p>
          <div className="mt-4 h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={revenue} barSize={28}>
                <CartesianGrid vertical={false} stroke="#e3dacb" />
                <XAxis dataKey="label" tickLine={false} axisLine={false} tick={{ fill: "#6d6458", fontSize: 12 }} />
                <YAxis
                  tickLine={false}
                  axisLine={false}
                  width={48}
                  tick={{ fill: "#6d6458", fontSize: 12 }}
                  tickFormatter={(value: number) => `R${Math.round(value / 1000)}k`}
                />
                <Tooltip
                  cursor={{ fill: "rgba(20,32,51,0.04)" }}
                  formatter={(value) => formatZar(Number(value ?? 0))}
                  contentStyle={{ borderRadius: 12, borderColor: "#e3dacb", background: "#fffdf9" }}
                />
                <Bar dataKey="collected" fill="#1e5c40" radius={[7, 7, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="rounded-2xl border border-line bg-card p-5 xl:col-span-2">
          <h2 className="font-display text-2xl">Occupancy</h2>
          <p className="mt-1 text-sm text-muted">Available units against those already let.</p>
          {properties.length === 0 ? (
            <p className="mt-10 text-sm text-muted">Add a property to see the split.</p>
          ) : (
            <>
              <div className="relative h-52">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={slices} dataKey="value" nameKey="name" innerRadius={58} outerRadius={80} paddingAngle={3} stroke="none">
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
              <ul className="mt-2 flex justify-center gap-4 text-sm text-muted">
                <li className="flex items-center gap-2">
                  <span className="h-2.5 w-2.5 rounded-full bg-moss" /> Occupied
                </li>
                <li className="flex items-center gap-2">
                  <span className="h-2.5 w-2.5 rounded-full bg-brass" /> Available
                </li>
              </ul>
            </>
          )}
        </article>
      </section>

      <section className="grid gap-4 lg:grid-cols-2">
        <article className="rounded-2xl border border-line bg-card p-5">
          <div className="flex items-center justify-between gap-3">
            <h2 className="font-display text-2xl">Latest payments</h2>
            <Link to="/payments" className="text-sm text-brass-deep hover:underline">
              View all
            </Link>
          </div>
          <ul className="mt-4 divide-y divide-line">
            {recentPayments.length === 0 ? <li className="py-6 text-sm text-muted">No payments recorded.</li> : null}
            {recentPayments.map((payment) => {
              const lease = leases.find((item) => item.leaseId === payment.leaseId);
              return (
                <li key={payment.paymentId} className="flex items-center justify-between gap-3 py-3">
                  <div>
                    <p className="text-sm font-medium">{lease?.tenantName ?? payment.leaseId}</p>
                    <p className="text-xs text-muted">{formatDate(payment.paymentDate)} · {payment.paymentMethod}</p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="num text-sm font-medium">{formatZar(payment.amount)}</span>
                    <StatusBadge status={payment.status} />
                  </div>
                </li>
              );
            })}
          </ul>
        </article>

        <article className="rounded-2xl border border-line bg-card p-5">
          <div className="flex items-center justify-between gap-3">
            <h2 className="font-display text-2xl">Open maintenance</h2>
            <Link to="/maintenance" className="text-sm text-brass-deep hover:underline">
              Open board
            </Link>
          </div>
          <ul className="mt-4 space-y-3">
            {openWork.length === 0 ? <li className="py-6 text-sm text-muted">No open tickets.</li> : null}
            {openWork.map((ticket) => (
              <li key={ticket.requestId} className="rounded-xl border border-line px-3 py-3">
                <div className="flex items-start justify-between gap-3">
                  <p className="text-sm leading-6">{ticket.description}</p>
                  <StatusBadge status={ticket.status} />
                </div>
                <p className="mt-2 text-xs text-muted">
                  {formatDate(ticket.reportedDate)} · {formatZar(ticket.cost)}
                </p>
              </li>
            ))}
          </ul>
        </article>
      </section>

      {managers.length > 0 || agents.length > 0 ? (
        <section className="grid gap-4 md:grid-cols-2">
          <article className="rounded-2xl border border-line bg-card p-5">
            <h2 className="font-display text-2xl">Managers</h2>
            <ul className="mt-4 space-y-3">
              {managers.map((manager) => (
                <li key={manager.managerId} className="flex items-center justify-between gap-3 text-sm">
                  <div>
                    <p className="font-medium">{manager.name}</p>
                    <p className="text-xs text-muted">{manager.employeeId}</p>
                  </div>
                  <p className="text-muted">{manager.contactNumber}</p>
                </li>
              ))}
            </ul>
          </article>
          <article className="rounded-2xl border border-line bg-card p-5">
            <h2 className="font-display text-2xl">Agents</h2>
            <ul className="mt-4 space-y-3">
              {agents.map((agent) => (
                <li key={agent.agentId} className="flex items-center justify-between gap-3 text-sm">
                  <div>
                    <p className="font-medium">{agent.name}</p>
                    <p className="text-xs text-muted">{agent.email}</p>
                  </div>
                  <p className="text-muted">{agent.role}</p>
                </li>
              ))}
            </ul>
          </article>
        </section>
      ) : null}
    </div>
  );
}
