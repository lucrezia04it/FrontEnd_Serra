package serra.autenticazione;

import java.util.Scanner;

public class MainLogin {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        String role = LoginManager.login(user, pass);

        if (role == null) {
            System.out.println("Credenziali non valide.");
        } else if (role.equals("admin")) {
            System.out.println("Benvenuto ADMIN");
            AdminMenu.start();
        } else {
            System.out.println("Benvenuto UTENTE");
            UserMenu.start();
        }
    }
}
