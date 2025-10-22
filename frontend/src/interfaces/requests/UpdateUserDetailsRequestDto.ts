export interface UpdateUserDetailsRequestDto {
  nickname?: string;
  age?: number;
  preferredGenresIdentifiers?: number[];
  password?: string;
  base64Avatar?: string;
}
