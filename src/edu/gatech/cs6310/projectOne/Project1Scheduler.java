package edu.gatech.cs6310.projectOne;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class Project1Scheduler implements Scheduler {
	public ArrayList<String[]> courses;
	public ArrayList<String[]> semesters;
	public ArrayList<String[]> students;
	public GRBVar[][][] gurobiVars;
	public ArrayList<String[]> studentDemand;
	public ArrayList<String[]> courseDependencies;
	
	public Project1Scheduler (ArrayList<String[]> courses, ArrayList<String[]> semesters, ArrayList<String[]> courseDependencies) {
		this.courses = courses;
		this.semesters = semesters;
		this.courseDependencies = courseDependencies;
	}
	
	@Override
	public double calculateSchedule( String datafolder ) {
		this.studentDemand = getStudentDemand(datafolder);
		this.students = getStudents(datafolder);
		
		this.gurobiVars = new GRBVar[this.students.size()][this.courses.size()][this.semesters.size()];

		GRBEnv env;
		double objectiveValue = 0;
		try {
			env = new GRBEnv();
			env.set(GRB.IntParam.LogToConsole, 0);
			GRBModel model = new GRBModel(env);
			// Create the variables.
			for (int i = 0; i < (this.students.size()); i++) {
				for (int j = 0; j < (this.courses.size()); j++) {
					for (int k = 0; k < (this.semesters.size()); k++) {
						String var = String.format("%s_%s_%s", this.students.get(i)[0], this.courses.get(j)[0], this.semesters.get(k)[0]);
						this.gurobiVars[i][j][k] = model.addVar(0, 1, 0.0, GRB.BINARY, var);
					}
				}
			}
								
			// Add the X variable to represent the capacity of the largest class.
			GRBVar X = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "X");
			
			// Integrate new variables
			model.update();
			
			// Set the objective as minimizing X (the capacity of the largest class).
			for (int j = 0; j < (this.courses.size()); j++) {
				for (int k = 0; k < (this.semesters.size()); k++) {
					GRBLinExpr expr = new GRBLinExpr();
					for (int i = 0; i < (this.students.size()); i++) {
						expr.addTerm( 1, this.gurobiVars[i][j][k]);
					}
					model.addConstr(expr, GRB.LESS_EQUAL, X, "");
				}
			}
			GRBLinExpr exprX = new GRBLinExpr();
			exprX.addTerm(1, X);
			model.setObjective(exprX, GRB.MINIMIZE);
			
			// Add constraint that each student can only take up to two courses per semester.
			for (int i = 0; i < (this.students.size()); i++) {
				for (int k = 0; k < (this.semesters.size()); k++) {
					GRBLinExpr expr = new GRBLinExpr();
					for (int j = 0; j < (this.courses.size()); j++) {
						expr.addTerm( 1, this.gurobiVars[i][j][k]);
					}
					model.addConstr(expr, GRB.LESS_EQUAL, 2, "");
				}
			}
			
			// Add constraint about needing to take all the courses in student demand csv.
			for (String[] studentCourse : this.studentDemand) {
				int i = Integer.valueOf(studentCourse[0])-1;
				int j = Integer.valueOf(studentCourse[1])-1;
				GRBLinExpr linExpr = new GRBLinExpr();
				for (int k = 0; k < this.semesters.size(); k++) {
					linExpr.addTerm( 1, this.gurobiVars[i][j][k]);
				}
				model.addConstr(linExpr, GRB.EQUAL, 1, "");
			}
						
			// Add constraint about taking course only one time.
			for (int i = 0; i < (this.students.size()); i++) {
				for (int j = 0; j < (this.courses.size()); j++) {
					GRBLinExpr expr = new GRBLinExpr();
					for (int k = 0; k < (this.semesters.size()); k++) {
						expr.addTerm( 1, this.gurobiVars[i][j][k]);
					}
					model.addConstr(expr, GRB.LESS_EQUAL, 1, "");
				}
			}

			// Add constraint about prerequisites.
			for (int i = 0; i < (this.students.size()); i++) {	

				for (String[] courseDependency : this.courseDependencies) {
					GRBLinExpr expr1 = new GRBLinExpr();
					int prereqCourse = Integer.valueOf(courseDependency[0]) - 1;
					int dependentCourse = Integer.valueOf(courseDependency[1]) - 1;

					for (int k = 1 ; k < (this.semesters.size()-1); k++) {
						expr1.addTerm( 1, this.gurobiVars[i][dependentCourse][k+1]);
						expr1.addTerm( -1, this.gurobiVars[i][prereqCourse][k]);
					}			
					model.addConstr(expr1, GRB.LESS_EQUAL, 0, "");
					
//					Add constraint that a student must take the prereq to take the dependent course.
					for (String[] studentDemand : this.studentDemand ) {
						int courseTheStudentWantsToTake = Integer.valueOf(studentDemand[1]) - 1;
						if (dependentCourse == courseTheStudentWantsToTake) {
							GRBLinExpr expr2 = new GRBLinExpr();
							for (int k = 1 ; k < (this.semesters.size()-1); k++) {
								expr2.addTerm( 1, this.gurobiVars[i][prereqCourse][k]);
							}			
							model.addConstr(expr2, GRB.EQUAL, 1, "");
						}
					}
				}
			}			
			
			// Add constraint about course availability.
			for (String[] course : this.courses) {
				int j = Integer.valueOf(course[0]) - 1;
				int fallSemesterAvailability = Integer.valueOf(course[3]);
				int springSemesterAvailability = Integer.valueOf(course[4]);
				int summerSemesterAvailability = Integer.valueOf(course[5]);

				for (int i = 0; i < (this.students.size()); i++) {
					for (int k = 0 ; k < (this.semesters.size()); k++) {
						GRBLinExpr linearExpr = new GRBLinExpr();
						linearExpr.addTerm( 1, this.gurobiVars[i][j][k]);
						if ((k % 3) == 0) {
							if (fallSemesterAvailability == 0) {
								model.addConstr(linearExpr, GRB.EQUAL, 0, "");
							} 
						} else if ((k % 3) == 1) {
							if (springSemesterAvailability == 0) {
								model.addConstr(linearExpr, GRB.EQUAL, 0, "");
							} 
						} else if ((k % 3) == 2) {
							if (summerSemesterAvailability == 0) {
								model.addConstr(linearExpr, GRB.EQUAL, 0, "");
							} 
						}
					}
				}
			}
			
			// Optimize the model
			model.optimize();

			// Display results
			objectiveValue = X.get(GRB.DoubleAttr.X);
			
			for (int i = 0; i < gurobiVars.length; i++) {
				for (int j = 0; j < gurobiVars[0].length; j++) {
					for (int k = 0; k < gurobiVars[0][0].length; k++) {
						if (gurobiVars[i][j][k].get(GRB.DoubleAttr.X) == 1 ) {
//							System.out.printf( "%03d is taking %03d in semester %03d\n", i, j, k );							
						}
					}
				}
			}
		} catch (GRBException e) {
			e.printStackTrace();
		} finally {
		}
		return objectiveValue;
	}
	
	// private method to get a unique list of students
	private ArrayList<String[]> getStudents(String datafolder) {
		ArrayList<String[]> students = new ArrayList<String[]>();
		
		for (int i = 1; i < this.studentDemand.size(); i++) {
			if (i == 1) {
				students.add(this.studentDemand.get(i));
			}
			if (i > 0 && (this.studentDemand.get(i-1)[0].equals(this.studentDemand.get(i)[0]))) {
				continue;
			} else {
				students.add(this.studentDemand.get(i));
			}
		}
		
		return students;
	}

	private ArrayList<String[]> getStudentDemand(String datafolder) {		
		BufferedReader br = null;
		String student = "";
		ArrayList<String[]> studentDemands = new ArrayList<String[]>();
//		String[] studentDemand1 = new String[3];
//		String[] studentDemand2 = new String[3];
//		studentDemand1[0] = "1";
//		studentDemand1[1] = "2";
//		studentDemand1[2] = "1";
//		studentDemand2[0] = "2";
//		studentDemand2[1] = "2";
//		studentDemand2[2] = "2";
//		studentDemands.add(studentDemand1);
//		studentDemands.add(studentDemand2);
//		
		try {
			br = new BufferedReader(new FileReader(datafolder));
			br.readLine();
			while ((student = br.readLine()) != null) {				
				studentDemands.add(student.split(","));
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
		return studentDemands;		
	}	
}
