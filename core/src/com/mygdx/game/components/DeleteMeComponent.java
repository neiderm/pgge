package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

public class DeleteMeComponent implements Component {

  public boolean deleteMe;

  public DeleteMeComponent(boolean deleteMe){
    this.deleteMe = deleteMe;
  }
  public DeleteMeComponent(){
      this(false);
  }
}
