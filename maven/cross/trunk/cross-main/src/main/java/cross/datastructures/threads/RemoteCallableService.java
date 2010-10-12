/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: RemoteCallableService.java 81 2010-01-06 18:15:16Z nilshoffmann $
 */
package cross.datastructures.threads;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Callable;


/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class RemoteCallableService implements IRemoteCallable {

        @Override
        public <T> T invoke(Callable<T> c) throws RemoteException {
            try {
                return c.call();
            } catch (Exception ex) {
                throw new RemoteException("Caught exception while running callable!",ex);
            }
        }

        public static void main(String[] args) {
            if (System.getSecurityManager() == null) {
                System.out.println("Creating new security manager!");
                System.setSecurityManager(new SecurityManager());
            }
            try {
                String name = "maltcmsRemoteCallableService";
                RemoteCallableService service = new RemoteCallableService();
                IRemoteCallable stub =
                    (IRemoteCallable) UnicastRemoteObject.exportObject(service, 0);
                Registry registry = LocateRegistry.getRegistry();
                registry.rebind(name, stub);
                System.out.println("ComputeEngine bound");
            } catch (Exception e) {
                System.err.println("ComputeEngine exception:");
                e.printStackTrace();
            }
        }


}
