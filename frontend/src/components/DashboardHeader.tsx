import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Input } from '@/components/ui/input';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { useAuth } from '@/contexts/AuthContext';
import {
  BookCheck,
  BookOpen,
  BookUp,
  CircleUser,
  FilePlus,
  Film,
  History,
  LogOut,
  Menu,
  Search,
  Sparkles,
  Ticket,
} from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';
import { FilterGroup } from './FilterGroup';

export function DashboardHeader({
  searchPhrase,
  onSearchChange,
  selectedGenres,
  onGenreChange,
}: Partial<{
  searchPhrase: string;
  onSearchChange: (phrase: string) => void;
  selectedGenres: number[];
  onGenreChange: (genreId: number, checked: boolean) => void;
}>) {
  const { logout, isAdmin, user } = useAuth();
  const pathname = useLocation().pathname;
  const shouldShowFilterGroup = pathname === '/dashboard';

  const userInitials = user ? user.nickname.slice(0, 2).toUpperCase() : 'U';

  const avatarTimestamp = localStorage.getItem('avatarTimestamp') || Date.now().toString();
  const avatarUrl = user?.avatarPath
    ? `${import.meta.env.VITE_BACKEND_URL}${user.avatarPath}?t=${avatarTimestamp}`
    : undefined;

  const userLinks = [
    {
      to: '/reservations',
      label: 'Moje rezerwacje',
      icon: <Ticket className="mr-2 h-4 w-4" />,
    },
    {
      to: '/rentals',
      label: 'Moje wypożyczenia',
      icon: <Film className="mr-2 h-4 w-4" />,
    },
    {
      to: '/recommendations',
      label: 'Moje rekomendacje',
      icon: <Sparkles className="mr-2 h-4 w-4" />,
    },
    {
      to: '/transactions-history',
      label: 'Historia wypożyczeń',
      icon: <History className="mr-2 h-4 w-4" />,
    },
  ];

  const adminLinks = [
    {
      to: '/admin/dvd/create',
      label: 'Dodaj nowe DVD',
      icon: <FilePlus className="mr-2 h-4 w-4" />,
    },
    {
      to: '/admin/reservations',
      label: 'Zarządzanie rezerwacjami',
      icon: <BookCheck className="mr-2 h-4 w-4" />,
    },
    {
      to: '/admin/returns',
      label: 'Zarządzanie zwrotami',
      icon: <BookUp className="mr-2 h-4 w-4" />,
    },
    {
      to: '/admin/genres',
      label: 'Zarządzanie gatunkami',
      icon: <BookOpen className="mr-2 h-4 w-4" />,
    },
  ];

  return (
    <header className="flex h-14 items-center gap-4 border-b bg-muted/40 px-4 lg:h-[60px] lg:px-6">
      <Sheet>
        <SheetTrigger asChild>
          <Button variant="outline" size="icon" className="shrink-0 md:hidden">
            <Menu className="h-5 w-5" />
            <span className="sr-only">Toggle navigation menu</span>
          </Button>
        </SheetTrigger>
        <SheetContent side="left" className="flex flex-col">
          <nav className="grid gap-2 text-lg font-medium p-2">
            <div className="mb-8">
              <h1 className="text-2xl font-bold flex items-center animate-pulse select-none p-2">
                <span className="text-indigo-500">O</span>pasRent
              </h1>
            </div>
            {shouldShowFilterGroup && (
              <FilterGroup isMobile selectedGenres={selectedGenres} onGenreChange={onGenreChange} />
            )}
          </nav>
        </SheetContent>
      </Sheet>
      <div className="w-full flex-1">
        {shouldShowFilterGroup && (
          <form onSubmit={(e) => e.preventDefault()}>
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                type="search"
                placeholder="Szukaj filmów..."
                className="w-full appearance-none bg-background pl-8 shadow-none md:w-2/3 lg:w-1/3"
                value={searchPhrase ?? ''}
                onChange={(e) => onSearchChange?.(e.target.value)}
              />
            </div>
          </form>
        )}
      </div>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="secondary"
            size="icon"
            className="rounded-full cursor-pointer p-0 overflow-hidden"
          >
            <Avatar className="h-9 w-9">
              <AvatarImage src={avatarUrl} alt={user?.nickname} />
              <AvatarFallback className="bg-indigo-500 text-white">{userInitials}</AvatarFallback>
            </Avatar>
            <span className="sr-only">Toggle user menu</span>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuLabel>Moje konto</DropdownMenuLabel>
          <DropdownMenuSeparator />
          {(isAdmin ? adminLinks : userLinks).map((link) => (
            <DropdownMenuItem key={link.to} asChild>
              <Link to={link.to}>
                {link.icon}
                {link.label}
              </Link>
            </DropdownMenuItem>
          ))}
          <DropdownMenuSeparator />
          <DropdownMenuItem asChild>
            <Link to="/profile">
              <CircleUser className="mr-2 h-4 w-4" />
              Mój profil
            </Link>
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem
            onClick={logout}
            className="text-red-500 focus:text-red-500 focus:bg-red-500/10"
          >
            <LogOut className="mr-2 h-4 w-4" />
            Wyloguj
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </header>
  );
}
