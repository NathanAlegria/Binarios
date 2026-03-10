/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package archivos_Binarios;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Nathan
 */
public class EmpleadoManager {

    /*
    Formato:
    1-File Codigos.emp:
    int code -> 4 bytes Mantener
    
    2- File Empleados.emp:
    int code
    String name
    double salary
    long fechaContratacion
    long fecha Despido
     */
    private RandomAccessFile rcods, remps;

    public EmpleadoManager() {
        try {
            File mf = new File("company");
            mf.mkdir();

            rcods = new RandomAccessFile("company/codigos.emp", "rw");
            remps = new RandomAccessFile("company/empleados.emp", "rw");

            //Inicializar un dato para el archivo de codigo
            initCode();

        } catch (IOException e) {
            System.out.println("Existe un Error");
        }
    }

    //Inizializa el codigo
    private void initCode() throws IOException {
        if (rcods.length() == 0) {
            rcods.writeInt(1);
        }
    }

    //Reubica el puntero a 0 consigue el numero Reubica a 0 y devuelve code con un valor mas
    private int getCode() throws IOException {
        rcods.seek(0);
        int code = rcods.readInt();
        rcods.seek(0);
        rcods.writeInt(code + 1);
        return code;
    }

    public void addEmployee(String name, double salary) throws IOException {
        remps.seek(remps.length());
        int code = getCode();
        //Asigna los valores de las cosas solicitadas
        remps.writeInt(code);
        remps.writeUTF(name);
        remps.writeDouble(salary);
        remps.writeLong(Calendar.getInstance().getTimeInMillis());
        remps.writeLong(0);
        //Crear Folder del Empleado
        createEmployeeFolders(code);
    }

    private String employeeFolder(int code) {
        return "company/empleado" + code;
    }

    private RandomAccessFile salesFileFor(int code) throws IOException {
        String dirPadre = employeeFolder(code);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String dir = dirPadre + "/ventas" + year + ".emp";
        return new RandomAccessFile(dir, "rw");
    }

    /*
    Formato VentasYear.emp
    double Saldo
    boolean estadodePago
     */
    private void createYearSalesFileFor(int code) throws IOException {
        RandomAccessFile rventa = salesFileFor(code);
        if (rventa.length() == 0) {
            for (int mes = 0; mes < 12; mes++) {
                rventa.writeDouble(0);
                rventa.writeBoolean(false);
            }
        }
    }

    private void createEmployeeFolders(int code) throws IOException {
        File dir = new File(employeeFolder(code));
        dir.mkdir();
        createYearSalesFileFor(code);
    }

    public void employeeList() throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int code = remps.readInt();
            String name = remps.readUTF();
            double salary = remps.readDouble();
            Date date = new Date(remps.readLong());
            if (remps.readLong() == 0) {
                System.out.println(code + " - " + name + " - " + salary + "$ - " + date);
            }

        }
    }

    //Valida Empleado Activo
    private boolean isEmployeeActive(int code) throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {

            int c = remps.readInt();
            long pos = remps.getFilePointer();
            remps.readUTF();
            remps.skipBytes(16);

            if (remps.readLong() == 0 && c == code) {
                remps.seek(pos);
                return true;
            }
        }
        return false;
    }

    //Agregar Venta(Mes actual)
    public void addSaleTO(int code, double ven) throws IOException {
        if (isEmployeeActive(code)) {
            RandomAccessFile sales = salesFileFor(code);
            int pos = Calendar.getInstance().get(Calendar.MONTH) * 9;
            sales.seek(pos);
            double monto = sales.readDouble();
            sales.seek(pos);
            sales.writeDouble(ven + monto);
        }

    }

    //Despedir Empleado
    public boolean fireEmployee(int code) throws IOException {
        if (isEmployeeActive(code)) {
            String name = remps.readUTF();
            remps.skipBytes(16);
            remps.writeLong(new Date().getTime());
            System.out.println("Despidiendo a: " + name);
            return true;
        }
        return false;
    }

    //Archivo de recibos de un empleado
    private RandomAccessFile billsFileFor(int code) throws IOException {
        String dir = employeeFolder(code) + "/recibos.emp";
        return new RandomAccessFile(dir, "rw");
    }

    //Pagar Empleado
    public void payEmployee(int code) throws IOException {
        if (!isEmployeeActive(code)) {
            System.out.println("No se pudo Pagar (Empleado inexistente o no Activo)");
            return;
        }

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int mes = Calendar.getInstance().get(Calendar.MONTH);
        int pos = mes * 9;

        RandomAccessFile sales = salesFileFor(code);
        createYearSalesFileFor(code);

        sales.seek(pos);
        double ventas = sales.readDouble();
        boolean pagado = sales.readBoolean();

        if (pagado) {
            System.out.println("No se pudo Pagar (El mes ya fue pagado)");
            return;
        }

        remps.seek(0);
        String name = "";
        double salary = 0;

        while (remps.getFilePointer() < remps.length()) {

            int cod = remps.readInt();
            String nam = remps.readUTF();
            double sal = remps.readDouble();
            remps.readLong();
            long despido = remps.readLong();

            if (cod == code && despido == 0) {
                name = nam;
                salary = sal;
                break;
            }
        }

        double sueldo = salary + (ventas * 0.10);
        double deduccion = sueldo * 0.035;
        double total = sueldo - deduccion;

        RandomAccessFile bill = billsFileFor(code);
        bill.seek(bill.length());

        bill.writeLong(new Date().getTime());
        bill.writeDouble(sueldo);
        bill.writeDouble(deduccion);
        bill.writeInt(year);
        bill.writeInt(mes);

        sales.seek(pos + 8);
        sales.writeBoolean(true);

        System.out.println("Empleado " + name + " se le pago Lps. " + total);
    }

    //Verifica si esta Pagado
    public boolean isEmployeePayed(int code) throws IOException {
        RandomAccessFile sales = salesFileFor(code);
        int mes = Calendar.getInstance().get(Calendar.MONTH);
        int pos = mes * 9;
        sales.seek(pos);
        sales.skipBytes(8);
        boolean pagado = sales.readBoolean();
        return pagado;
    }

    //Imprimir info
    public void printEmployee(int code) throws IOException {

        remps.seek(0);

        String name = "";
        double salary = 0;
        Date fechaContratacion = null;

        boolean found = false;

        while (remps.getFilePointer() < remps.length()) {

            int c = remps.readInt();
            String n = remps.readUTF();
            double s = remps.readDouble();
            Date fecha = new Date(remps.readLong());
            long despido = remps.readLong();

            if (c == code) {
                name = n;
                salary = s;
                fechaContratacion = fecha;
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Empleado no existe");
            return;
        }

        //Paso 1
        System.out.println("Codigo: " + code);
        System.out.println("Nombre: " + name);
        System.out.println("Salario: " + salary);
        System.out.println("Fecha de contratacion: " + fechaContratacion);

        //Paso 2
        RandomAccessFile sales = salesFileFor(code);

        sales.seek(0);

        double totalVentas = 0;

        for (int mes = 0; mes < 12; mes++) {

            double ventas = sales.readDouble();
            sales.readBoolean();

            System.out.println("Mes " + (mes + 1) + " : " + ventas);

            totalVentas += ventas;
        }

        //Paso 3
        System.out.println("Total de ventas del año: " + totalVentas);

        //Paso 4
        RandomAccessFile bills = billsFileFor(code);

        bills.seek(0);

        int count = 0;

        while (bills.getFilePointer() < bills.length()) {

            bills.readLong();
            bills.readDouble();
            bills.readDouble();
            bills.readInt();
            bills.readInt();

            count++;
        }

        System.out.println("Total de pagos realizados: " + count);
    }
}
