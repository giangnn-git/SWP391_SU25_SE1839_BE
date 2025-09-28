package vn.hoidanit.jobhunter.domain;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "ServiceCenter")
public class ServiceCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 20 characters")
    private String name;

    @NotBlank(message = "Address cannot be blank")
    @Size(min = 5, max = 100, message = "Address must be between 5 and 100 characters")
    private String address;

    @Pattern(regexp = "^\\d{10}$", message = "Phone must be ten numberics")
    private String phoneNumber;

    @OneToMany(mappedBy = "serviceCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WarrantyClaim> warrantyClaims = new HashSet<>();

    @OneToMany(mappedBy = "serviceCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "serviceCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SCExpense> scExpenses = new HashSet<>();

    @OneToMany(mappedBy = "serviceCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PartInventory> partInventories = new HashSet<>();

    public ServiceCenter() {

    }

    public ServiceCenter(String name, String address, String phoneNumber) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Set<WarrantyClaim> getWarrantyClaims() {
        return warrantyClaims;
    }

    public void setWarrantyClaims(Set<WarrantyClaim> warrantyClaims) {
        this.warrantyClaims = warrantyClaims;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<SCExpense> getScExpenses() {
        return scExpenses;
    }

    public void setScExpenses(Set<SCExpense> scExpenses) {
        this.scExpenses = scExpenses;
    }

    public Set<PartInventory> getPartInventories() {
        return partInventories;
    }

    public void setPartInventories(Set<PartInventory> partInventories) {
        this.partInventories = partInventories;
    }

    @Override
    public String toString() {
        return "ServiceCenter [id=" + id + ", name=" + name + ", address=" + address + ", phoneNumber=" + phoneNumber
                + "]";
    }
}
