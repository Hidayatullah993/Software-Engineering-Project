/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 * Student is uniquely identified by its macAddress
 * @author shenying
 */
public class Student {

    /**
     * instance variable of macAddress
     */
    private String macAddress;

    /**
     * instance variable of school
     */
    private String school;

    /**
     * instance variable of year
     */
    private String year;

    /**
     * instance variable of gender 
     */
    private String gender;
    
    /**
     * This the constructor for Student which consists of macAddress, email, gender 
     * @param macAddress macAddress of Student
     * @param email email of Student
     * @param gender gender of Student
     */
    public Student(String macAddress, String email, String gender){
        this.macAddress = macAddress;
        this.gender = gender;
        
        String[] emailSplit = email.split("@");
        String username = emailSplit[0];
        String domain = emailSplit[1];
        year = username.substring(username.lastIndexOf(".") + 1);
        //System.out.println(domain.split("[.]")[0]);
        school = domain.split("[.]")[0];
    }
    
    /**
     * Get student MacAddress 
     * @return macAddress of the student
     */
    public String getMacAddress() {
        return macAddress;
    }
    
    /**
     * Get school of the student
     * @return school of the student
     */
    public String getSchool() {
        return school;
    }
    
    /**
     * Get year in which student is in
     * @return year in which student is in
     */
    public String getYear() {
        return year;
    }
    
    /**
     * Get gender of the student
     * @return gender of the student
     */
    public String getGender() {
        return gender;
    }
}
