package com.github.techChallenge.domain.user;

import com.github.techChallenge.domain.usertype.UserType;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class User {

    private static final Pattern EMAIL_LOCAL_PART = Pattern.compile(
            "[\\p{L}\\p{N}!#$%&'*+/=?^_`{|}~-]+"
                    + "(?:\\.[\\p{L}\\p{N}!#$%&'*+/=?^_`{|}~-]+)*"
    );

    private static final Pattern EMAIL_DOMAIN = Pattern.compile(
            "[\\p{L}\\p{N}](?:[\\p{L}\\p{N}-]{0,61}[\\p{L}\\p{N}])?"
                    + "(?:\\.[\\p{L}\\p{N}]"
                    + "(?:[\\p{L}\\p{N}-]{0,61}[\\p{L}\\p{N}])?)*"
    );

    private static final Pattern ZIP_CODE_PATTERN =
            Pattern.compile("[0-9]{5}-?[0-9]{3}");

    private static final Pattern STATE_PATTERN =
            Pattern.compile("[A-Z]{2}");

    private Long id;
    private String name;
    private String email;
    private String login;
    private String password;
    private UserLevel userLevel;
    private UserType type;
    private Address address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(
            Long id,
            String name,
            String email,
            String login,
            String password,
            UserLevel userLevel,
            UserType type,
            Address address,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.login = login;
        this.password = password;
        this.userLevel = userLevel;
        this.type = type;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User create(
            String name,
            String email,
            String login,
            String password,
            UserLevel userLevel,
            Address address
    ) {
        email = normalize(email);
        login = normalize(login);

        validateCreation(
                name,
                email,
                login,
                password,
                userLevel,
                address
        );

        LocalDateTime now = LocalDateTime.now();

        return new User(
                null,
                name,
                email,
                login,
                password,
                userLevel,
                null,
                address,
                now,
                now
        );
    }

    private static String normalize(String value) {
        return value == null
                ? null
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private static void validateCreation(
            String name,
            String email,
            String login,
            String password,
            UserLevel userLevel,
            Address address
    ) {
        Map<String, String> violations = validateCommonData(
                name, email, login, userLevel, address
        );

        if (requireText(
                violations,
                "password",
                password,
                "A senha é obrigatória."
        ) && (password.length() < 8 || password.length() > 100)) {
            violations.put(
                    "password",
                    "A senha deve possuir entre 8 e 100 caracteres."
            );
        }

        throwIfInvalid(violations);
    }

    private static Map<String, String> validateCommonData(
            String name,
            String email,
            String login,
            UserLevel userLevel,
            Address address
    ) {
        Map<String, String> violations = new LinkedHashMap<>();

        if (requireText(
                violations,
                "name",
                name,
                "O nome é obrigatório."
        ) && name.length() > 50) {
            violations.put(
                    "name",
                    "O nome deve possuir no máximo 50 caracteres."
            );
        }

        if (requireText(
                violations,
                "email",
                email,
                "O e-mail é obrigatório."
        )) {
            if (email.length() > 255) {
                violations.put(
                        "email",
                        "O e-mail deve possuir no máximo 255 caracteres."
                );
            } else if (!isValidEmail(email)) {
                violations.put(
                        "email",
                        "O e-mail possui formato inválido."
                );
            }
        }

        if (requireText(
                violations,
                "login",
                login,
                "O login é obrigatório."
        ) && (login.length() < 4 || login.length() > 100)) {
            violations.put(
                    "login",
                    "O login deve possuir entre 4 e 100 caracteres."
            );
        }

        if (userLevel == null) {
            violations.put(
                    "userLevel",
                    "O nível de usuário é obrigatório."
            );
        }

        validateAddress(address, violations);

        return violations;
    }

    private static void throwIfInvalid(Map<String, String> violations) {
        if (!violations.isEmpty()) {
            throw new InvalidUserDataException(violations);
        }
    }

    private static boolean requireText(
            Map<String, String> violations,
            String field,
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            violations.put(field, message);
            return false;
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        int separator = email.indexOf('@');

        return separator > 0
                && separator <= 64
                && EMAIL_LOCAL_PART
                .matcher(email.substring(0, separator))
                .matches()
                && EMAIL_DOMAIN
                .matcher(email.substring(separator + 1))
                .matches();
    }

    private static void validateAddress(
            Address address,
            Map<String, String> violations
    ) {
        if (address == null) {
            violations.put(
                    "address",
                    "O endereço é obrigatório."
            );
            return;
        }

        requireText(
                violations,
                "address.address",
                address.address(),
                "O logradouro é obrigatório."
        );

        requireText(
                violations,
                "address.number",
                address.number(),
                "O número é obrigatório."
        );

        requireText(
                violations,
                "address.neighborhood",
                address.neighborhood(),
                "O bairro é obrigatório."
        );

        requireText(
                violations,
                "address.city",
                address.city(),
                "A cidade é obrigatória."
        );

        requireText(
                violations,
                "address.country",
                address.country(),
                "O país é obrigatório."
        );

        if (requireText(
                violations,
                "address.zipCode",
                address.zipCode(),
                "O CEP é obrigatório."
        ) && !ZIP_CODE_PATTERN.matcher(address.zipCode()).matches()) {
            violations.put(
                    "address.zipCode",
                    "O padrão para o CEP é 99999-999 ou 99999999."
            );
        }

        if (requireText(
                violations,
                "address.state",
                address.state(),
                "A UF é obrigatória."
        ) && !STATE_PATTERN.matcher(address.state()).matches()) {
            violations.put(
                    "address.state",
                    "A UF deve possuir duas letras maiúsculas."
            );
        }
    }

    public void update(
            String name,
            String email,
            String login,
            UserLevel userLevel,
            Address address
    ) {
        email = normalize(email);
        login = normalize(login);

        Map<String, String> violations = validateCommonData(
                name, email, login, userLevel, address
        );
        throwIfInvalid(violations);

        this.name = name;
        this.email = email;
        this.login = login;
        this.address = address;
        this.userLevel = userLevel;
        this.updatedAt = LocalDateTime.now();
    }

    public void encodePassword(UnaryOperator<String> encoder) {
        this.password = encoder.apply(this.password);
    }

    public void changePassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    public void grantType(UserType userType) {
        this.type = userType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public UserType getType() {
        return type;
    }

    public Address getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UserLevel getUserLevel() {
        return userLevel;
    }
}
