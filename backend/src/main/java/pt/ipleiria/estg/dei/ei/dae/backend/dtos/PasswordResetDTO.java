package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

public class PasswordResetDTO {
    private String password ;

    public PasswordResetDTO() {
    }

    public PasswordResetDTO(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
