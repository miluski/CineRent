import { Button } from '@/components/ui/button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
  onPageChange: (page: number) => void;
}

export const Pagination = ({
  currentPage,
  totalPages,
  totalElements,
  pageSize,
  hasNext,
  hasPrevious,
  onPageChange,
}: PaginationProps) => {
  const startItem = currentPage * pageSize + 1;
  const endItem = Math.min((currentPage + 1) * pageSize, totalElements);

  const renderPageNumbers = () => {
    const pages = [];
    const maxVisiblePages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage < maxVisiblePages - 1) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    if (startPage > 0) {
      pages.push(
        <Button
          key={0}
          variant={currentPage === 0 ? 'default' : 'outline'}
          onClick={() => onPageChange(0)}
          className="min-w-8 sm:min-w-10 h-8 sm:h-10 text-xs sm:text-sm px-2 sm:px-3"
        >
          1
        </Button>
      );
      if (startPage > 1) {
        pages.push(
          <span key="start-ellipsis" className="px-1 sm:px-2 text-xs sm:text-sm">
            ...
          </span>
        );
      }
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <Button
          key={i}
          variant={currentPage === i ? 'default' : 'outline'}
          onClick={() => onPageChange(i)}
          className="min-w-8 sm:min-w-10 h-8 sm:h-10 text-xs sm:text-sm px-2 sm:px-3"
        >
          {i + 1}
        </Button>
      );
    }

    if (endPage < totalPages - 1) {
      if (endPage < totalPages - 2) {
        pages.push(
          <span key="end-ellipsis" className="px-1 sm:px-2 text-xs sm:text-sm">
            ...
          </span>
        );
      }
      pages.push(
        <Button
          key={totalPages - 1}
          variant={currentPage === totalPages - 1 ? 'default' : 'outline'}
          onClick={() => onPageChange(totalPages - 1)}
          className="min-w-8 sm:min-w-10 h-8 sm:h-10 text-xs sm:text-sm px-2 sm:px-3"
        >
          {totalPages}
        </Button>
      );
    }

    return pages;
  };

  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-3 sm:gap-4 border-t border-gray-700 pt-4 mt-8">
      <div className="text-xs sm:text-sm text-gray-400 text-center sm:text-left">
        Wyświetlanie: {startItem} - {endItem} z {totalElements} wyników
      </div>
      <div className="flex items-center gap-1 sm:gap-2 flex-wrap justify-center">
        <Button
          variant="outline"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={!hasPrevious}
          className="flex items-center gap-1 h-8 sm:h-10 px-2 sm:px-4 text-xs sm:text-sm"
        >
          <ChevronLeft className="w-3 h-3 sm:w-4 sm:h-4" />
          <span className="hidden sm:inline">Poprzednia</span>
          <span className="sm:hidden">Poprz.</span>
        </Button>
        <div className="flex items-center gap-1">{renderPageNumbers()}</div>
        <Button
          variant="outline"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={!hasNext}
          className="flex items-center gap-1 h-8 sm:h-10 px-2 sm:px-4 text-xs sm:text-sm"
        >
          <span className="hidden sm:inline">Następna</span>
          <span className="sm:hidden">Nast.</span>
          <ChevronRight className="w-3 h-3 sm:w-4 sm:h-4" />
        </Button>
      </div>
    </div>
  );
};
