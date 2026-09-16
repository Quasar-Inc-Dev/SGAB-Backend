package sgab.sgab.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import sgab.sgab.entities.Usuario;

public class UsuarioDetails implements UserDetails {
    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario){
        this.usuario = usuario;
    }

    @Override 
    public String getUsername(){
        return usuario.getEmail();
    }

    @Override 
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override 
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().name()));
    }

    @Override 
    public boolean isEnabled(){
        return usuario.getStatusUsuario();
    }

    @Override 
    public boolean isAccountNonExpired() {return true;}

    @Override 
    public boolean isAccountNonLocked() {return true;}

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    public Usuario getUsuario() {
        return usuario;
    }
}
