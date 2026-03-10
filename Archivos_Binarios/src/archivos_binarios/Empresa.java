/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

package archivos_binarios;

import archivos_Binarios.EmpleadoManager;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Nathan
 */
 
public class Empresa {

    public static void main(String[] args) {

        Scanner lea = new Scanner(System.in);
        EmpleadoManager manager = new EmpleadoManager();

        int op = 0;

        do {

            System.out.println("\n*********** MENU PRINCIPAL ***********");
            System.out.println("1- Agregar Empleado");
            System.out.println("2- Listar Empleado No Despedido");
            System.out.println("3- Agregar Venta a Empleado");
            System.out.println("4- Pagar Empleado");
            System.out.println("5- Despedir Empleado");
            System.out.println("6- Salir");
            System.out.print("Escoja una opción: ");

            op = lea.nextInt();
            try {

                switch (op) {

                    case 1:
                        System.out.print("Nombre del empleado: ");
                        String name = lea.nextLine();

                        System.out.print("Salario: ");
                        double salary = lea.nextDouble();

                        manager.addEmployee(name, salary);

                        System.out.println("Empleado agregado correctamente.");
                        break;

                    case 2:
                        manager.employeeList();
                        break;

                    case 3:
                        System.out.print("Codigo del empleado: ");
                        int codeVenta = lea.nextInt();

                        System.out.print("Monto de la venta: ");
                        double venta = lea.nextDouble();

                        manager.addSaleTO(codeVenta, venta);

                        System.out.println("Venta agregada.");
                        break;

                    case 4:
                        System.out.print("Codigo del empleado: ");
                        int codePago = lea.nextInt();

                        manager.payEmployee(codePago);
                        break;

                    case 5:
                        System.out.print("Codigo del empleado a despedir: ");
                        int codeFire = lea.nextInt();

                        if (manager.fireEmployee(codeFire)) {
                            System.out.println("Empleado despedido.");
                        } else {
                            System.out.println("No se pudo despedir.");
                        }

                        break;

                    case 6:
                        System.out.println("Saliendo del sistema...");
                        break;

                    default:
                        System.out.println("Opcion no valida.");
                }

            } catch (IOException e) {
                System.out.println("Error en el sistema: " + e.getMessage());
            }

        } while (op != 6);
    }
}
