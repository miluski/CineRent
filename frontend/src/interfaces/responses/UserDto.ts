export interface UserDto {
  nickname: string;
  email: string;
  isVerified: boolean;
  age: number;
  preferredGenres: string[];
  avatarPath?: string;
}
