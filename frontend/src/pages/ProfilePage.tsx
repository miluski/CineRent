import { DashboardHeader } from '@/components/DashboardHeader';
import { DashboardSidebar } from '@/components/DashboardSidebar';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { useAuth } from '@/contexts/AuthContext';
import { useResendVerification } from '@/hooks/mutations/useResendVerification';
import { useUpdateUserDetails } from '@/hooks/mutations/useUpdateUserDetails';
import { useVerifyEmail } from '@/hooks/mutations/useVerifyEmail';
import { useGetAllGenres } from '@/hooks/queries/useGetAllGenres';
import type { UpdateUserDetailsRequestDto } from '@/interfaces/requests/UpdateUserDetailsRequestDto';
import { zodResolver } from '@hookform/resolvers/zod';
import { Camera, CheckCircle, Mail, XCircle } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import * as z from 'zod';

const formSchema = z.object({
  nickname: z
    .string()
    .min(3, 'Nazwa użytkownika musi mieć co najmniej 3 znaki.')
    .max(50, 'Nazwa użytkownika nie może być dłuższa niż 50 znaków.'),
  password: z
    .string()
    .min(8, 'Hasło musi mieć co najmniej 8 znaków.')
    .max(100, 'Hasło nie może być dłuższe niż 100 znaków.')
    .optional()
    .or(z.literal('')),
  age: z.coerce
    .number<number>()
    .min(18, 'Wiek musi być większy od 18.')
    .max(149, 'Wiek nie może być większy niż 149.'),
  preferredGenresIdentifiers: z.array(z.number()).optional(),
});

export function ProfilePage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { data: genres } = useGetAllGenres();
  const [avatarTimestamp, setAvatarTimestamp] = useState(
    localStorage.getItem('avatarTimestamp') || Date.now().toString()
  );
  const updateUserMutation = useUpdateUserDetails(() => {
    form.reset({ ...form.getValues(), password: '' });
    setAvatarPreview(null);
    setAvatarTimestamp(localStorage.getItem('avatarTimestamp') || Date.now().toString());
  });
  const resendVerificationMutation = useResendVerification();
  const verifyEmailMutation = useVerifyEmail();

  const [showVerificationModal, setShowVerificationModal] = useState(false);
  const [verificationCode, setVerificationCode] = useState('');
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);

  const handleResendVerification = () => {
    if (!user?.email) {
      toast.error('Brak adresu email.');
      return;
    }

    resendVerificationMutation.mutate(
      { email: user.email },
      {
        onSuccess: () => {
          toast.success('Kod weryfikacyjny został wysłany na Twój email.');
          setShowVerificationModal(true);
          setVerificationCode('');
        },
        onError: () => {
          toast.error('Nie udało się wysłać kodu. Spróbuj ponownie.');
        },
      }
    );
  };

  const handleVerification = () => {
    if (!user?.email) {
      toast.error('Brak adresu email.');
      return;
    }

    if (verificationCode.length !== 6) {
      toast.error('Kod weryfikacyjny musi mieć 6 cyfr.');
      return;
    }

    verifyEmailMutation.mutate(
      { email: user.email, code: verificationCode },
      {
        onSuccess: () => {
          toast.success('Email został pomyślnie zweryfikowany!');
          setShowVerificationModal(false);
          setVerificationCode('');
        },
        onError: () => {
          toast.error('Nieprawidłowy kod weryfikacyjny.');
        },
      }
    );
  };

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      nickname: user?.nickname ?? '',
      password: '',
      age: user?.age ?? 18,
      preferredGenresIdentifiers:
        (user?.preferredGenres
          ?.map((genreName) => genres?.find((g) => g.name === genreName)?.id)
          .filter(Boolean) as number[]) ?? [],
    },
  });

  const handleAvatarChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      toast.error('Rozmiar pliku nie może przekraczać 5MB');
      return;
    }

    if (!file.type.startsWith('image/')) {
      toast.error('Plik musi być obrazem');
      return;
    }

    const reader = new FileReader();
    reader.onloadend = () => {
      setAvatarPreview(reader.result as string);
    };
    reader.readAsDataURL(file);
  };

  async function onSubmit(values: z.infer<typeof formSchema>) {
    const payload: UpdateUserDetailsRequestDto = {};

    if (values.nickname !== user?.nickname) {
      payload.nickname = values.nickname;
    }

    if (values.password && values.password !== '') {
      payload.password = values.password;
    }

    if (values.age !== user?.age) {
      payload.age = values.age;
    }

    const currentGenreIds =
      user?.preferredGenres
        ?.map((genreName) => genres?.find((g) => g.name === genreName)?.id)
        .filter(Boolean)
        .sort() ?? [];
    const newGenreIds = (values.preferredGenresIdentifiers ?? []).sort();

    if (JSON.stringify(currentGenreIds) !== JSON.stringify(newGenreIds)) {
      payload.preferredGenresIdentifiers = newGenreIds;
    }

    if (avatarPreview) {
      payload.base64Avatar = avatarPreview;
    }

    if (Object.keys(payload).length === 0) {
      toast.info('Nie wprowadzono żadnych zmian.');
      return;
    }

    updateUserMutation.mutate(payload);
  }

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6">
          <div className="flex items-center animate-in fade-in-0 slide-in-from-bottom-4 duration-700 fill-mode-backwards">
            <h1 className="text-lg font-semibold md:text-2xl">Informacje o koncie</h1>
          </div>
          <Card
            className={
              'animate-in fade-in-0 slide-in-from-bottom-4 duration-700 delay-100 fill-mode-backwards'
            }
          >
            <CardHeader>
              <CardTitle>Edytuj swój profil</CardTitle>
              <CardDescription>
                Zmień swoje dane i preferencje. Kliknij "Zakończ edycję", aby zapisać.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Form {...form}>
                <form
                  onSubmit={form.handleSubmit(onSubmit)}
                  className="grid grid-cols-1 gap-8 md:grid-cols-3"
                >
                  <div className="flex flex-col items-center gap-4 md:col-span-1">
                    <Avatar className="h-40 w-40">
                      <AvatarImage
                        draggable="false"
                        src={
                          avatarPreview ||
                          (user?.avatarPath
                            ? `${import.meta.env.VITE_BACKEND_URL}${
                                user.avatarPath
                              }?t=${avatarTimestamp}`
                            : undefined)
                        }
                      />
                      <AvatarFallback>
                        {user?.nickname?.substring(0, 2).toUpperCase()}
                      </AvatarFallback>
                    </Avatar>
                    <p className="font-semibold">{user?.nickname}</p>
                    <Button
                      type="button"
                      variant="outline"
                      className="w-full"
                      onClick={() => document.getElementById('avatar-upload')?.click()}
                    >
                      <Camera className="mr-2 h-4 w-4" />
                      Zmień profilowe
                    </Button>
                    <input
                      id="avatar-upload"
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={handleAvatarChange}
                    />
                  </div>

                  <div className="space-y-4 md:col-span-2">
                    <FormField
                      control={form.control}
                      name="nickname"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Nickname</FormLabel>
                          <FormControl>
                            <Input {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="password"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Nowe hasło (opcjonalnie)</FormLabel>
                          <FormControl>
                            <Input
                              type="password"
                              placeholder="Zostaw puste, jeśli bez zmian"
                              {...field}
                            />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="age"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Wiek</FormLabel>
                          <FormControl>
                            <Input type="number" {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <div>
                      <FormLabel>Edytuj preferencje filmowe</FormLabel>
                      <div className="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4">
                        {genres?.map((genre) => (
                          <FormField
                            key={genre.id}
                            control={form.control}
                            name="preferredGenresIdentifiers"
                            render={({ field }) => (
                              <FormItem className="flex flex-row items-center space-x-3 space-y-0">
                                <FormControl>
                                  <Checkbox
                                    checked={field.value?.includes(genre.id)}
                                    onCheckedChange={(checked) => {
                                      return checked
                                        ? field.onChange([...(field.value ?? []), genre.id])
                                        : field.onChange(
                                            field.value?.filter((value) => value !== genre.id)
                                          );
                                    }}
                                  />
                                </FormControl>
                                <FormLabel className="font-normal">{genre.name}</FormLabel>
                              </FormItem>
                            )}
                          />
                        ))}
                      </div>
                    </div>
                  </div>

                  <div className="flex flex-col gap-2 md:col-span-3 md:flex-row md:justify-end">
                    <Button type="submit" disabled={updateUserMutation.isPending}>
                      {updateUserMutation.isPending ? 'Zapisywanie...' : 'Zakończ edycję danych'}
                    </Button>
                    <Button type="button" variant="outline" onClick={() => navigate(-1)}>
                      Powrót
                    </Button>
                  </div>
                </form>
              </Form>
            </CardContent>
          </Card>

          <Card
            className={
              'animate-in fade-in-0 slide-in-from-bottom-4 duration-700 delay-200 fill-mode-backwards'
            }
          >
            <CardHeader>
              <CardTitle>Weryfikacja email</CardTitle>
              <CardDescription>Status weryfikacji Twojego adresu email</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  {user?.isVerified ? (
                    <>
                      <CheckCircle className="h-5 w-5 text-green-600" />
                      <div>
                        <p className="font-medium">Email zweryfikowany</p>
                        <p className="text-sm text-gray-500">{user.email}</p>
                      </div>
                      <Badge className="bg-green-100 text-green-800 hover:bg-green-100">
                        Zweryfikowany
                      </Badge>
                    </>
                  ) : (
                    <>
                      <XCircle className="h-5 w-5 text-red-600" />
                      <div>
                        <p className="font-medium">Email niezweryfikowany</p>
                        <p className="text-sm text-gray-500">{user?.email}</p>
                      </div>
                      <Badge variant="destructive">Niezweryfikowany</Badge>
                    </>
                  )}
                </div>
                {!user?.isVerified && (
                  <Button
                    onClick={handleResendVerification}
                    disabled={resendVerificationMutation.isPending}
                    variant="outline"
                  >
                    <Mail className="mr-2 h-4 w-4" />
                    {resendVerificationMutation.isPending
                      ? 'Wysyłanie...'
                      : 'Wyślij kod weryfikacyjny'}
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>

          <Dialog open={showVerificationModal} onOpenChange={setShowVerificationModal}>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Weryfikacja email</DialogTitle>
                <DialogDescription>
                  Wprowadź 6-cyfrowy kod weryfikacyjny wysłany na adres {user?.email}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <label className="mb-2 block text-sm font-medium">Kod weryfikacyjny</label>
                  <Input
                    type="text"
                    maxLength={6}
                    value={verificationCode}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      setVerificationCode(value);
                    }}
                    placeholder="123456"
                    className="text-center text-2xl tracking-widest"
                  />
                </div>
                <div className="flex gap-2">
                  <Button
                    onClick={handleVerification}
                    disabled={verificationCode.length !== 6 || verifyEmailMutation.isPending}
                    className="flex-1"
                  >
                    {verifyEmailMutation.isPending ? 'Weryfikacja...' : 'Zweryfikuj'}
                  </Button>
                  <Button
                    onClick={handleResendVerification}
                    disabled={resendVerificationMutation.isPending}
                    variant="outline"
                    className="flex-1"
                  >
                    {resendVerificationMutation.isPending ? 'Wysyłanie...' : 'Wyślij ponownie'}
                  </Button>
                </div>
              </div>
            </DialogContent>
          </Dialog>
        </main>
      </div>
    </div>
  );
}
