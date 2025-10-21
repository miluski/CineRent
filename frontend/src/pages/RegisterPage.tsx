import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { ConfettiButton } from '@/components/ui/confetti';
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
import { Meteors } from '@/components/ui/meteors';
import { useRegister } from '@/hooks/mutations/auth/useRegister';
import { useResendVerification } from '@/hooks/mutations/useResendVerification';
import { useVerifyEmail } from '@/hooks/mutations/useVerifyEmail';
import { useGetAllGenres } from '@/hooks/queries/useGetAllGenres';
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import * as z from 'zod';

const formSchema = z.object({
  nickname: z
    .string()
    .min(3, 'Nazwa użytkownika musi mieć co najmniej 3 znaki.')
    .max(50, 'Nazwa użytkownika nie może być dłuższa niż 50 znaków.'),
  email: z.string().email('Nieprawidłowy adres email.').min(1, 'Email jest wymagany.'),
  password: z
    .string()
    .min(8, 'Hasło musi mieć co najmniej 8 znaków.')
    .max(100, 'Hasło nie może być dłuższe niż 100 znaków.'),
  age: z.coerce
    .number<number>()
    .min(18, 'Wiek musi być większy od 18.')
    .max(149, 'Wiek nie może być większy niż 149.'),
  preferredGenresIdentifiers: z.array(z.number()).optional(),
});

export function RegisterPage() {
  const registerMutation = useRegister();
  const verifyEmailMutation = useVerifyEmail();
  const resendVerificationMutation = useResendVerification();
  const navigate = useNavigate();
  const [isSuccess, setIsSuccess] = useState(false);
  const [showVerificationModal, setShowVerificationModal] = useState(false);
  const [verificationCode, setVerificationCode] = useState('');
  const [userEmail, setUserEmail] = useState('');
  const { data: genres } = useGetAllGenres();

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      nickname: '',
      email: '',
      password: '',
      age: 18,
      preferredGenresIdentifiers: [],
    },
  });

  function onSubmit(values: z.infer<typeof formSchema>) {
    const payload = {
      ...values,
      preferredGenresIdentifiers: values.preferredGenresIdentifiers || [],
    };

    registerMutation.mutate(payload, {
      onSuccess: () => {
        setUserEmail(values.email);
        setShowVerificationModal(true);
        toast.success(
          'Rejestracja zakończona! Sprawdź swoją skrzynkę email i wprowadź kod weryfikacyjny.'
        );
      },
      onError: (error: any) => {
        console.error('Registration failed', error);
        if (error.response?.status === 422) {
          toast.error('Użytkownik z tym adresem email lub nickiem już istnieje.');
        } else if (error.response?.status === 500) {
          toast.error('Błąd serwera. Spróbuj ponownie później.');
        } else {
          toast.error('Rejestracja nie powiodła się. Spróbuj ponownie.');
        }
      },
    });
  }

  function handleVerification() {
    if (!verificationCode || verificationCode.length !== 6) {
      toast.error('Kod weryfikacyjny musi mieć 6 cyfr.');
      return;
    }

    verifyEmailMutation.mutate(
      { email: userEmail, code: verificationCode },
      {
        onSuccess: () => {
          setIsSuccess(true);
          setShowVerificationModal(false);
          toast.success('Email zweryfikowany pomyślnie! Możesz się teraz zalogować.');
          setTimeout(() => {
            navigate('/login', { state: { verified: true } });
          }, 1500);
        },
        onError: () => {
          toast.error('Nieprawidłowy kod weryfikacyjny lub kod wygasł.');
        },
      }
    );
  }

  function handleResendCode() {
    resendVerificationMutation.mutate(
      { email: userEmail },
      {
        onSuccess: () => {
          toast.success('Nowy kod weryfikacyjny został wysłany na Twój email.');
        },
        onError: () => {
          toast.error('Nie udało się wysłać kodu. Spróbuj ponownie.');
        },
      }
    );
  }

  return (
    <div className="flex flex-col md:flex-row min-h-screen">
      <Meteors />
      <div
        className="relative w-full md:w-1/2 bg-slate-900 text-white p-8 flex flex-col bg-cover bg-center"
        style={{
          backgroundImage:
            "linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.7)), url('/auth-overlay.jpg')",
        }}
      >
        <div className="mb-8">
          <h1 className="text-2xl font-bold flex items-center animate-pulse select-none">
            <span className="text-indigo-500">O</span>pasRent
          </h1>
        </div>
        <div className="mt-auto mb-24">
          <h2 className="text-2xl md:text-4xl font-bold leading-snug">
            "Twoje wszystkie filmy, które szukasz bez szukania."
          </h2>
        </div>
      </div>
      <div className="w-full md:w-1/2 bg-white p-8 flex items-center justify-center">
        <div className="w-full max-w-md space-y-6">
          <div className="text-center">
            <h2 className="text-3xl font-bold mb-2">Zarejestruj się</h2>
            <p className="text-gray-600 text-sm">
              Jedno kliknięcie - i już jesteś w filmowym świecie
            </p>
          </div>

          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="nickname"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nickname</FormLabel>
                    <FormControl>
                      <Input {...field} className="w-full" />
                    </FormControl>
                    <FormMessage className="h-5" />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Email</FormLabel>
                    <FormControl>
                      <Input type="email" {...field} className="w-full" />
                    </FormControl>
                    <FormMessage className="h-5" />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Hasło</FormLabel>
                    <FormControl>
                      <Input type="password" {...field} className="w-full" />
                    </FormControl>
                    <FormMessage className="h-5" />
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
                      <Input
                        type="number"
                        {...field}
                        min={18}
                        className="w-full"
                        onChange={(e) => {
                          field.onChange(e.target.valueAsNumber);
                        }}
                      />
                    </FormControl>
                    <FormMessage className="h-5" />
                  </FormItem>
                )}
              />

              <div className="space-y-2">
                <FormLabel>Preferencje filmowe</FormLabel>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                  {genres?.map((genre) => (
                    <div key={genre.id} className="flex items-center space-x-2">
                      <Checkbox
                        id={`genre-${genre.id}`}
                        onCheckedChange={(checked) => {
                          const currentValues = form.getValues().preferredGenresIdentifiers || [];

                          const newValues = checked
                            ? [...currentValues, genre.id]
                            : currentValues.filter((id) => id !== genre.id);

                          form.setValue('preferredGenresIdentifiers', newValues);
                        }}
                      />
                      <label
                        htmlFor={`genre-${genre.id}`}
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                      >
                        {genre.name}
                      </label>
                    </div>
                  ))}
                </div>
              </div>

              <ConfettiButton
                type="submit"
                className="w-full bg-black hover:bg-gray-800 text-white"
                disabled={registerMutation.isPending || isSuccess}
                active={isSuccess}
              >
                {registerMutation.isPending ? 'Rejestrowanie...' : 'Utwórz konto'}
              </ConfettiButton>
            </form>
          </Form>
          <div className="relative flex items-center">
            <div className="flex-grow border-t border-gray-300"></div>
            <span className="flex-shrink mx-4 text-gray-500 text-sm">lub</span>
            <div className="flex-grow border-t border-gray-300"></div>
          </div>
          <Link to="/login" className="block w-full">
            <Button variant="outline" className="w-full text-black border-black">
              Zaloguj się
            </Button>
          </Link>
        </div>
      </div>

      <Dialog
        open={showVerificationModal}
        onOpenChange={(open) => {
          if (!open && !isSuccess) {
            toast.info(
              'Możesz zweryfikować email później. Weryfikacja jest obowiązkowa aby ustawiać powiadomienia o dostępności filmów.'
            );
            navigate('/login');
          }
          setShowVerificationModal(open);
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Weryfikacja adresu email</DialogTitle>
            <DialogDescription>
              Wysłaliśmy 6-cyfrowy kod weryfikacyjny na adres {userEmail}. Wprowadź go poniżej, aby
              zweryfikować swoje konto.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                Kod weryfikacyjny
              </label>
              <Input
                type="text"
                maxLength={6}
                placeholder="123456"
                value={verificationCode}
                onChange={(e) => setVerificationCode(e.target.value.replace(/\D/g, ''))}
                className="w-full text-center text-2xl tracking-widest mt-2"
              />
            </div>
            <div className="flex flex-col gap-2">
              <Button
                onClick={handleVerification}
                disabled={verifyEmailMutation.isPending || isSuccess}
                className="w-full"
              >
                {verifyEmailMutation.isPending ? 'Weryfikowanie...' : 'Weryfikuj'}
              </Button>
              <Button
                variant="outline"
                onClick={handleResendCode}
                disabled={resendVerificationMutation.isPending}
                className="w-full"
              >
                {resendVerificationMutation.isPending ? 'Wysyłanie...' : 'Wyślij ponownie kod'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
