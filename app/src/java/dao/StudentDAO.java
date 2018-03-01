package dao;

import entity.Student;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * StudentDAO will be used for breakdown of Year 2013/2014/2015/2016/2017 students, genders, and schools
 * @author shenying
 */
public class StudentDAO {

    /**
     * instance variable of a list of student
     */
    private ArrayList<Student> studentList;

    /**
     * instance variable of a list of school
     */
    private ArrayList<String> schoolList;

    /**
     * instance variable of a list of gender
     */
    private ArrayList<String> genderList;

    /**
     * instance variable of a list of year
     */
    private ArrayList<String> yearList;

    /**
     * instantiate the list of student
     * instantiate the list of school
     * instantiate the list of gender
     * instantiate the list of year
     */
    public StudentDAO() {
        studentList = new ArrayList<>();
        schoolList = new ArrayList<>(Arrays.asList("accountancy", "business", "economics", "law", "sis", "socsc"));
        genderList = new ArrayList<>(Arrays.asList("M", "F"));
        yearList = new ArrayList<>(Arrays.asList("2013", "2014", "2015", "2016", "2017"));
    }

    /**
     * method to add Student Objects into the studentList
     * @param stu student object
     */
    public void addStudent(Student stu) {
        studentList.add(stu);
    }

    /**
     * This method returns all Student Objects in the list
     * @return returns studentList
     */
    public ArrayList<Student> getAllStudents() {
        return studentList;
    }

    /**
     * This method breaks down all students based on year as first criteria
     * 
     * @return Returns a Map where key is the breakdown by the years and value is the ArrayList of Student objects assigned to that breakdown
     */
    public Map<String, ArrayList<Student>> firstBreakdownByYear() {
        Map<String, ArrayList<Student>> result = new TreeMap<>();

        for (String yearStr : yearList) {
            result.put(yearStr, new ArrayList<>());
        }

        for (Student student : studentList) {
            String year = student.getYear();
            ArrayList<Student> list = result.get(year);
            list.add(student);
            result.put(year, list);
        }
        return result;
    }

    /**
     * This method return the firstBreakdownByGender 
     * The return variable is a Map with the gender as the key and an ArrayList
     * of Student 
     * @return the map of gender and a list of student
     */
    public Map<String, ArrayList<Student>> firstBreakdownByGender() {
        Map<String, ArrayList<Student>> result = new TreeMap<>(Collections.reverseOrder());

        for (String genderStr : genderList) {
            result.put(genderStr, new ArrayList<>());
        }

        for (Student student : studentList) {
            String gender = student.getGender();
            ArrayList<Student> list = result.get(gender);
            list.add(student);
            result.put(gender, list);
        }
        return result;
    }

    /**
     * This method return firstBreadownBySchool
     * The return variable is a Map with the school as the key and an ArrayList
     * of Student
     * @return the map of school and a list of student
     */
    public Map<String, ArrayList<Student>> firstBreakdownBySchool() {
        Map<String, ArrayList<Student>> result = new TreeMap<>();

        for (String schoolStr : schoolList) {
            result.put(schoolStr, new ArrayList<>());
        }

        for (Student student : studentList) {
            String school = student.getSchool();
            ArrayList<Student> list = result.get(school);
            list.add(student);
            result.put(school, list);
        }
        return result;
    }

    /**
     * This method takes in the first layer breakdown Map and adds the second layer breakdown by year
     * @param firstMap First layer breakdown Map
     * 
     * @return 2-layered breakdown map with second criteria as year
     */
    public Map<String, Map<String, ArrayList<Student>>> secondBreakdownByYear(Map<String, ArrayList<Student>> firstMap) {
        Map<String, Map<String, ArrayList<Student>>> result = new TreeMap<>();
        Set<String> firstSet = firstMap.keySet();
        for (String firstBreakdown : firstSet) {
            ArrayList<Student> firstList = firstMap.get(firstBreakdown);
            Map<String, ArrayList<Student>> secondMap = new TreeMap<>();

            for (String yearStr : yearList) {
                secondMap.put(yearStr, new ArrayList<>());
            }

            for (Student student : firstList) {
                String year = student.getYear();
                ArrayList<Student> list = secondMap.get(year);
                list.add(student);
                secondMap.put(year, list);
            }
            result.put(firstBreakdown, secondMap);
        }
        return result;
    }

    /**
     * This method takes in the first layer breakdown Map and adds the second layer breakdown by gender
     * @param firstMap First layer breakdown Map
     * @return 2-layered breakdown map with second criteria as gender
     */
    public Map<String, Map<String, ArrayList<Student>>> secondBreakdownByGender(Map<String, ArrayList<Student>> firstMap) {
        Map<String, Map<String, ArrayList<Student>>> result = new TreeMap<>();
        Set<String> firstSet = firstMap.keySet();
        for (String firstBreakdown : firstSet) {
            ArrayList<Student> firstList = firstMap.get(firstBreakdown);
            Map<String, ArrayList<Student>> secondMap = new TreeMap<>(Collections.reverseOrder());

            for (String genderStr : genderList) {
                secondMap.put(genderStr, new ArrayList<>());
            }

            for (Student student : firstList) {
                String gender = student.getGender();
                ArrayList<Student> list = secondMap.get(gender);
                list.add(student);
                secondMap.put(gender, list);
            }
            result.put(firstBreakdown, secondMap);
        }
        return result;
    }

    /**
     * This method takes in the first layer breakdown Map and adds the second layer breakdown by school
     * @param firstMap First layer breakdown Map
     * @return 2-layered breakdown map with second criteria as school
     */
    public Map<String, Map<String, ArrayList<Student>>> secondBreakdownBySchool(Map<String, ArrayList<Student>> firstMap) {
        Map<String, Map<String, ArrayList<Student>>> result = new TreeMap<>();
        Set<String> firstSet = firstMap.keySet();
        for (String firstBreakdown : firstSet) {
            ArrayList<Student> firstList = firstMap.get(firstBreakdown);
            Map<String, ArrayList<Student>> secondMap = new TreeMap<>();

            for (String schoolStr : schoolList) {
                secondMap.put(schoolStr, new ArrayList<>());
            }

            for (Student student : firstList) {
                String school = student.getSchool();
                ArrayList<Student> list = secondMap.get(school);
                list.add(student);
                secondMap.put(school, list);
            }
            result.put(firstBreakdown, secondMap);
        }
        return result;
    }

    /**
     * This method takes in the 2-layered breakdown map and adds the third breakdown by gender
     * @param secondMap 2-layered breakdown Map
     * @return 3-layered breakdown Map with third criteria as gender
     */
    public Map<String, Map<String, Map<String, ArrayList<Student>>>> thirdBreakdownByGender(Map<String, Map<String, ArrayList<Student>>> secondMap) {
        Map<String, Map<String, Map<String, ArrayList<Student>>>> result = new TreeMap<>();
        Set<String> firstSet = secondMap.keySet();
        for (String firstKey : firstSet) {
            //Map<String, ArrayList<Student>> thirdMap = new TreeMap<>();
            Map<String, Map<String, ArrayList<Student>>> tempInnerMap = new TreeMap<>();
            Map<String, ArrayList<Student>> innerMap = secondMap.get(firstKey);
            Set<String> innerSet = innerMap.keySet();
            for (String innerKey : innerSet) {
                Map<String, ArrayList<Student>> thirdMap = new TreeMap<>(Collections.reverseOrder());

                for (String genderStr : genderList) {
                    thirdMap.put(genderStr, new ArrayList<>());
                }

                ArrayList<Student> existingList = innerMap.get(innerKey);
                for (Student student : existingList) {
                    String gender = student.getGender();
                    ArrayList<Student> list = thirdMap.get(gender);
                    list.add(student);
                    thirdMap.put(gender, list);
                }
                tempInnerMap.put(innerKey, thirdMap);
            }
            result.put(firstKey, tempInnerMap);
        }
        return result;
    }

    /**
     * This method takes in the 2-layered breakdown map and adds the third breakdown by school
     * @param secondMap 2-layered breakdown Map
     * 
     * @return 3-layered breakdown Map with third criteria as school
     */
    public Map<String, Map<String, Map<String, ArrayList<Student>>>> thirdBreakdownBySchool(Map<String, Map<String, ArrayList<Student>>> secondMap) {
        Map<String, Map<String, Map<String, ArrayList<Student>>>> result = new TreeMap<>();
        Set<String> firstSet = secondMap.keySet();
        for (String firstKey : firstSet) {
            //Map<String, ArrayList<Student>> thirdMap = new TreeMap<>();
            Map<String, Map<String, ArrayList<Student>>> tempInnerMap = new TreeMap<>();
            Map<String, ArrayList<Student>> innerMap = secondMap.get(firstKey);
            Set<String> innerSet = innerMap.keySet();
            for (String innerKey : innerSet) {
                Map<String, ArrayList<Student>> thirdMap = new TreeMap<>();

                for (String schoolStr : schoolList) {
                    thirdMap.put(schoolStr, new ArrayList<>());
                }

                ArrayList<Student> existingList = innerMap.get(innerKey);
                for (Student student : existingList) {
                    String school = student.getSchool();
                    ArrayList<Student> list = thirdMap.get(school);
                    list.add(student);
                    thirdMap.put(school, list);
                }
                tempInnerMap.put(innerKey, thirdMap);
            }
            result.put(firstKey, tempInnerMap);
        }
        return result;
    }

    /**
     * This method takes in the 2-layered breakdown map and adds the third breakdown by year
     * @param secondMap 2-layered breakdown Map
     * @return 3-layered breakdown Map with third criteria as year
     */
    public Map<String, Map<String, Map<String, ArrayList<Student>>>> thirdBreakdownByYear(Map<String, Map<String, ArrayList<Student>>> secondMap) {
        Map<String, Map<String, Map<String, ArrayList<Student>>>> result = new TreeMap<>();
        Set<String> firstSet = secondMap.keySet();
        for (String firstKey : firstSet) {
            //Map<String, ArrayList<Student>> thirdMap = new TreeMap<>();
            Map<String, Map<String, ArrayList<Student>>> tempInnerMap = new TreeMap<>();
            Map<String, ArrayList<Student>> innerMap = secondMap.get(firstKey);
            Set<String> innerSet = innerMap.keySet();
            for (String innerKey : innerSet) {
                Map<String, ArrayList<Student>> thirdMap = new TreeMap<>();

                for (String yearStr : yearList) {
                    thirdMap.put(yearStr, new ArrayList<>());
                }

                ArrayList<Student> existingList = innerMap.get(innerKey);
                for (Student student : existingList) {
                    String year = student.getYear();
                    ArrayList<Student> list = thirdMap.get(year);
                    list.add(student);
                    thirdMap.put(year, list);
                }
                tempInnerMap.put(innerKey, thirdMap);
            }
            result.put(firstKey, tempInnerMap);
        }
        return result;
    }
}
