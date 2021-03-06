package com.digitalsanctum.idea.plugins.buildr.execution;

/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.digitalsanctum.idea.plugins.buildr.Buildr;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ComboboxSpeedSearch;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.SortedComboBoxModel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

class ModuleSelector {
  private static final String NO_MODULE = "<Project Root>";

  private final Project myProject;
  private final JComboBox myModulesList;
  private final SortedComboBoxModel<Object> myModules = new SortedComboBoxModel<Object>( new Comparator<Object>() {
    public int compare( final Object module, final Object module1 ) {
      if ( module instanceof Module && module1 instanceof Module ) {
        return ( ( Module ) module ).getName().compareToIgnoreCase( ( ( Module ) module1 ).getName() );
      }
      return -1;
    }
  } );

  public ModuleSelector( final Project project, final JComboBox modulesList ) {
    myProject = project;
    myModulesList = modulesList;
    new ComboboxSpeedSearch( modulesList ) {
      protected String getElementText( Object element ) {
        if ( element instanceof Module ) {
          return ( ( Module ) element ).getName();
        }
        else if ( element == null ) {
          return NO_MODULE;
        }
        return super.getElementText( element );
      }
    };
    myModulesList.setModel( myModules );
    myModulesList.setRenderer( new ListCellRendererWrapper() {
      @Override
      public void customize( final JList list, final Object value, final int index, final boolean selected, final boolean hasFocus ) {
        if ( value instanceof Module ) {
          final Module module = ( Module ) value;
          setIcon( ModuleType.get( module ).getNodeIcon( true ) );
          setText( module.getName() );
        }
        else if ( value == null ) {
          setText( NO_MODULE );
        }
      }
    } );
  }

  public void updateModules() {
    Module selectedModule = getModule();

    final Module[] modules = ModuleManager.getInstance( getProject() ).getModules();
    final List<Module> list = new ArrayList<Module>();
    for ( final Module module : modules ) {
      if ( isModuleAccepted( module ) ) {
        list.add( module );
      }
    }
    setModules( list );
    if ( list.contains( selectedModule ) ) {
      setSelectedModule( selectedModule );
    }
  }

  public boolean isModuleAccepted( final Module module ) {
    VirtualFile[] contentRoots = ModuleRootManager.getInstance( module ).getContentRoots();
    for ( int i = 0; i < contentRoots.length; i++ ) {
      if ( Buildr.buildfilePresent( contentRoots[ i ] ) ) {
        return true;
      }
    }
    return false;
  }

  public Project getProject() {
    return myProject;
  }

  public void setSelectedModule( Module aModule ) {
    myModules.setSelectedItem( aModule );
  }

  private void setModules( final Collection<Module> modules ) {
    myModules.clear();
    myModules.add( null );
    for ( Module module : modules ) {
      myModules.add( module );
    }
    if ( myModules.getSize() > 0 ) {
      myModulesList.setEnabled( true );
      setSelectedModule( ( Module ) myModules.get( 0 ) );
    }
    else {
      myModulesList.setEnabled( false );
      setSelectedModule( null );
    }
  }

  public Module getModule() {
    return ( Module ) myModules.getSelectedItem();
  }
}

