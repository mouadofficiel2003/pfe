export type RoleUtilisateur = "ADMINISTRATEUR" | "GESTIONNAIRE";

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
  username: string;
  role: RoleUtilisateur;
};
