# pgge

Pretty Good Game Engine

Started life as libGDX 3D and Bullet physics demo from "http://bedroomcoders.co.uk/libgdx-bullet-redux-2/", 
modified to Ashley ECS (entity component system architecture).

Present Features:


Features TBD:
- developer menu: SelectScreen, onESC, list availble .json from which to choose screen 
- android/assets: ./models (g3d[bj] ./screens (.json)  ./data (shared .pngs etc) ./sounds?
- new screen work (g*** ptrl)
- camera auto-panning/circling when no player input
- let camera 3rd pers. mode be also controlled by input mapper
- for effect for polygon appearing/removing from view (put a "bubble" (opaque) around the player/camera position if this help diffuse)
- debug panel can enable/disable layers or groups of object/geometery for troubleshooting

Code Improvement
- RenderSystem multi-instance ModelComp?
- status system track charactre position deltas and broadcast event at intervals i.e. when given position delta exceeded
  ... characters to maintain list of last 10 characters and positions reported (and sorted by proximity)
  ... characters to set track on nearest, track as long or until proximity delta exceeded .. then reset track to
         current nearest.
- bullet world entire game lifetime ?

Issues:
- SelectScreen position models  needs offset to look right because models have differnt centers
- make test objects conditional on bogus feature (on "test" Screen only)
- rigs to use "nodes" model for all ...
- ... and player rig explosion is stalling on model reload of separate nodes model- 
- wonky camera falling (apply more filter on trackerSB ) ... should be "lazier"
   (camera flipping has to do with settingt the tracker vector when rolling from side to side


Assets Used Info

Jeep Renegade 5 doors compact SUV 2016 
https://www.cgtrader.com/free-3d-models/car/suv/jeep-renegade-a-5-doors-compact-suv-from-2016
credit: Jose-Bronze (Icense Note: "Royalty Free License") 
Import 3DS
- unpack the archive and image files, import .3ds 
- import .3ds, leave default 'y forward' 'z up' 
- change 3d view to "texture" - model should renders w/ textures

s .4 ^a scale
r z 180  ^a rotation
r x -90  ^a rotation
r x 90         
export fbx 'Selected Objects' Scale: .01 (Z)forward (Y)up


RAM3500 by WTLion
https://www.turbosquid.com/3d-models/ram-3ds-free/491059
"free for personal and comercial use,requeriment only credits for author."
Import 3DS
s 1.4
ctrl+a s 
r z 180
ctrl+a r
r x -90
ctrl+a r
r x 90
Export FBX Z-Forward Y-Up Scale 0.01


Tankcar
Author: atze
https://opengameart.org/content/tankcar
license for model "panzerwagen" is CC0.
License(s): CC0
s .5 r z 180 r x -90 Ctrl+a o  (saves rotation & scale)
r x 90
Export FBX Z-Forward Y-Up Scale 0.01


APC Tank
Submitted by gabrielsdj (Personal Use License)
https://free3d.com/3d-model/apc-tank-42997.html
Reset tank w/ desired CG over grid center. Shift S 3D-curson to origin.
ctrl+shift+alt+c origin to 3D cursor
s .3 (.25?)  r x -90 ctrl+a s ctrl+a r ctrl+a location
r x 90
export FBX .01


Military van
Author:  johndh
https://opengameart.org/content/military-van
License(s): CC-BY-SA 3.0 GPL 3.0 GPL 2.0
Export OBJ Z-Forward Y-Up Scale 0.35

3D Low poly offroad vehicle by nice3dmodels
https://www.turbosquid.com/3d-models/3d-offroad-car-1328883
Open offroadcar.blend
Export OBJ Z-Forward Y-Up Scale 0.35

"Red Sand Texture"
https://www.flickr.com/photos/maleny_steve/8899498324


Things to investigate:
ComponentMapper
IteratingSystem
PooledEngine
