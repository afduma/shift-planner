export interface Team {
  id: string;
  name: string;
  description: string | null;
  active: boolean;
}

export interface TeamUpsertRequest {
  name: string;
  description: string | null;
  active: boolean;
}
