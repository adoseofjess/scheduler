package edu.gatech.cs6310.projectOne;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ProjectOne {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String coursesFile = "resources/static/courses.csv";
		String semestersFile = "resources/static/semesters.csv";
		String courseDependenciesFile = "resources/static/course_dependencies.csv";
		
		ArrayList<String[]> courses = getCourses(coursesFile);
		ArrayList<String[]> semesters = getSemesters(semestersFile);
		ArrayList<String[]> courseDependencies = getCourseDependencies(courseDependenciesFile);
		
		Project1Scheduler scheduler = new Project1Scheduler(courses, semesters, courseDependencies);
		double result = scheduler.calculateSchedule(args[0]);
		System.out.printf("X=%f", result);
	}

	private static ArrayList<String[]> getCourses(String datafolder) {
		
		BufferedReader br = null;
		String line = "";
		ArrayList<String[]> courses = new ArrayList<String[]>();
//		String[] course1 = new String[6];
//		String[] course2 = new String[6];
//		String[] course3 = new String[6];
//		course1[0] = "1";
//		course1[1] = "Blah";
//		course1[2] = "courseno";
//		course1[3] = "0";
//		course1[4] = "1";
//		course1[5] = "1";
//		course2[0] = "2";
//		course2[1] = "Blah";
//		course2[2] = "courseno";
//		course2[3] = "1";
//		course2[4] = "1";
//		course2[5] = "1";
//		course3[0] = "3";
//		course3[1] = "Blah";
//		course3[2] = "courseno";
//		course3[3] = "1";
//		course3[4] = "1";
//		course3[5] = "1";
//		courses.add(course1);
//		courses.add(course2);
//		courses.add(course3);
		
		try {
			br = new BufferedReader(new FileReader(datafolder));
			br.readLine();
			while ((line = br.readLine()) != null) {				
				courses.add(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return courses;
		
	}
	
	private static ArrayList<String[]> getSemesters(String datafolder) {
		
		BufferedReader br = null;
		String line = "";
		ArrayList<String[]> semesters = new ArrayList<String[]>();
//		String[] semester1 = new String[1];
//		String[] semester2 = new String[1];
//		String[] semester3 = new String[1];
//		semester1[0] = "1";
//		semester2[0] = "2";
//		semester3[0] = "3";
//		semesters.add(semester1);
//		semesters.add(semester2);
//		semesters.add(semester3);
		
		try {
			br = new BufferedReader(new FileReader(datafolder));
			br.readLine();
			while ((line = br.readLine()) != null) {				
				semesters.add(line.split(","));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return semesters;
	}

	private static ArrayList<String[]> getCourseDependencies(String datafolder) {
		
		BufferedReader br = null;
		String line = "";
		ArrayList<String[]> courseDependencies = new ArrayList<String[]>();
//		ArrayList<String[]> semesters = new ArrayList<String[]>();
//		String[] courseDependency1 = new String[2];
//		courseDependency1[0] = "1";
//		courseDependency1[1] = "2";
//		courseDependencies.add(courseDependency1);
		
		try {
			br = new BufferedReader(new FileReader(datafolder));
			br.readLine();
			while ((line = br.readLine()) != null) {				
				courseDependencies.add(line.split(","));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return courseDependencies;
	}
}
