import { Link, useLocation } from "react-router-dom";
import { FilterGroup } from "./FilterGroup";

export function DashboardSidebar() {
  const pathname = useLocation().pathname;

  return (
    <div className="hidden border-r bg-muted/40 md:block">
      <div className="flex h-full max-h-screen flex-col gap-2">
        <div className="flex h-14 items-center border-b px-4 lg:h-[60px] lg:px-6">
          <Link
            to="/dashboard"
            className="flex items-center gap-2 font-semibold"
          >
            <h1 className="text-2xl font-bold flex items-center animate-pulse select-none text-black">
              <span className="text-red-500">O</span>pasRent
            </h1>
          </Link>
        </div>
        <div className="flex-1">
          <nav className="grid items-start px-2 text-sm font-medium lg:px-4">
            {pathname === "/dashboard" && <FilterGroup />}
          </nav>
        </div>
      </div>
    </div>
  );
}
