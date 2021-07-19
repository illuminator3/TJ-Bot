```diff
- DO NOT
```

Store passwords in plaintext. You should never have a record of what your user's passwords are in a way which you can read them.

```diff
- DO NOT
```

Encrypt passwords.

<https://nakedsecurity.sophos.com/2013/11/04/anatomy-of-a-password-disaster-adobes-giant-sized-cryptographic-blunder/>

```diff
- DO NOT
```

Use a general purpose hash function like SHA or MD5.

<https://security.stackexchange.com/questions/90064/how-secure-are-sha256-salt-hashes-for-password-storage>

```diff
- DO NOT
```

"Roll your own" implementation of cryptographic functions. You'll get it wrong.

<https://security.stackexchange.com/questions/18197/why-shouldnt-we-roll-our-own>

```diff
+ DO
```

Use a well tested implementation of PBKDF2, bcrypt, or scrypt to hash and salt passwords.

<https://spring.io/projects/spring-security>
<https://mvnrepository.com/artifact/org.springframework.security/spring-security-core>

```java
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public final class PasswordUtils {
	private PasswordUtils() {}
    
    private static final PasswordEncoder PASSWORD_ENCODER = 
        // At time of writing the default implementation uses BCrypt
		PasswordEncoderFactories.createDelegatingPasswordEncoder();

	// Store the result of this in your database
	public static String encode(String password) {
		return PASSWORD_ENCODER.encode(password);
	}

	// And use this to check if a user gave you the right password later
	public static boolean matches(String password, String encodedPassword) {
		return PASSWORD_ENCODER.matches(password, encodedPassword);
	}
}
```
