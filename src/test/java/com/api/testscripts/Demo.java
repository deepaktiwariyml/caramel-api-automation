package com.api.testscripts;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Standard Functional Interfaces: Consumer, Supplier, Predicate, Function
 * Consumer- accept a argument , return nothing
 * Predicate- accept a argument , return a boolean
 * Supplier- accept nothing , return a Type Parameter
 * Function- accept a Type Parameter as a Argument , return a Type Parameter
 */
public class Demo  {



    public int sum(int a,int b) {

            return a+b;

    }

    public static void showTypeInference() {


    }

    public void reflectionDemo() throws ClassNotFoundException {
        Class myClass=Class.forName(this.getClass().getCanonicalName());
        Method[] methods=myClass.getDeclaredMethods();
        for (Method m:methods){
            System.out.println(m.getName());
        }
    }

    public static void main(String[] args) {

        // Anonymous Runnable
             Runnable r1 = new Runnable(){

               @Override
                     public void run(){
                     System.out.println("Hello world one!");
                   }
                };

             // Lambda Runnable
             Runnable r2 = () -> System.out.println("Hello world two!");

             // Run em!
             r1.run();
             r2.run();


        JButton testButton = new JButton("Test Button");
        testButton.addActionListener(new ActionListener() {
                                         @Override
                                         public void actionPerformed(ActionEvent e) {
                                             System.out.println("Action Performed with event "+e);
                                         }
                                     }
        );

        testButton.addActionListener(e->System.out.println("Action Performed with event "+e));



        Comparator oldIdComparator = (c1,c2) -> {
            Employee emp1=(Employee)c1;
            Employee emp2=(Employee)c2;
            return emp1.getId().compareTo(emp2.getId());
        };


        Comparator aicIdComparator=new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {

                Employee emp1=(Employee)o1;
                Employee emp2=(Employee)o2;
                return emp1.getId().compareTo(emp2.getId());
            }
        };

        Comparator lambdaComparator=(o1,o2) -> 1;

        Comparator idComparator=Comparator.comparing(Employee::getId);
        Comparator nameComparator=Comparator.comparing(Employee::getName);
        Employee emp1=new Employee(1,"Deepak");
        Employee emp2=new Employee(2,"Amit");
        Employee emp3=new Employee(4,"Payal");
        Employee emp4=new Employee(3,"Jamal");
        Employee emp5=new Employee(99,"Amin");
        Employee emp6=new Employee(101,"Ashray");
        Employee emp7=new Employee(6,"Manoj");
        List<Employee> empList=new ArrayList<>();
        empList.add(emp1);
        empList.add(emp2);
        empList.add(emp3);
        empList.add(emp4);
        empList.add(emp5);
        empList.add(emp6);
        empList.add(emp7);


        Collections.sort(empList,oldIdComparator);
      //  Collections.reverse(empList);
        System.out.println("Sort By Id: "+empList);

        Collections.sort(empList,nameComparator);
        System.out.println("Sort By Name: "+empList);

        Consumer<Employee> printConsumer=(e)->{
            System.out.println("Employees "+e);
        };

        Function<Employee,String> empToStringFunction=(e)->{
          return e.getId().toString();
        };

        empList.forEach(printConsumer);

        Predicate<Employee> startsWithFilter=employee -> {
            return employee.getName().startsWith("A");
        };

        List<Employee> employeesStartWithA=empList.stream().filter(startsWithFilter).collect(Collectors.toList());
        employeesStartWithA.forEach(employee -> {
            System.out.println("EMPLOYEE DETAILS "+employee.id+ " " +employee.name);
        });

        List<Integer> idList=employeesStartWithA.stream().
                map(employee -> employee.getId()).
                collect(Collectors.toList());
        employeesStartWithA.stream().
                flatMapToInt(employee-> IntStream.of(employee.getId())).forEach(System.out::println);

        List<String> nameList=empList.stream().
                filter(employee -> employee.getName().startsWith("A")).
                map(Employee::getName).
                collect(Collectors.toList());

        List<Integer> idList1=empList.stream().
                filter(employee -> employee.getName().startsWith("A")).
                map(Employee::getId).
                collect(Collectors.toList());

        List<String> idListUsingFunction=empList.stream().
                filter(startsWithFilter).
                map(empToStringFunction).collect(Collectors.toList());

        System.out.println("ID List Using Function"+idListUsingFunction);
        System.out.println("ID List "+idList);
        Demo d=new Demo();
        try {
            d.reflectionDemo();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        showTypeInference();
        int[] a = {10, 45, 99, 6, 199};
        int i;
        int largest = 0, secondlargest = -1;
        for (i = 1; i < a.length; i++) {
            if (a[i] > a[largest]) {
                largest = i;
            }


            for (int j = 0; j < a.length; j++) {
                if (a[j] != a[largest]) {
                    if (secondlargest == -1)
                        secondlargest = i;
                    else if (a[i] > a[secondlargest])
                        secondlargest = i;

                }
            }

        }
        System.out.println("Second Largest " + a[secondlargest]);
    }


}

class Employee{
    Integer id;
    String name;


    Employee(int id,String name){
        this.id=id;
        this.name=name;

    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return this.getId()+ "-"+this.getName();
    }
}