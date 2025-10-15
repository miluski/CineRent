import { LoaderIcon } from "lucide-react";
import * as React from "react";

import { cn } from "@/lib/utils";

const Spinner = React.forwardRef<SVGSVGElement, React.ComponentProps<"svg">>(
  ({ className, ...props }, ref) => {
    return (
      <LoaderIcon
        ref={ref}
        className={cn("animate-spin", className)}
        {...props}
      />
    );
  }
);
Spinner.displayName = "Spinner";

export { Spinner };
