# pgge

Pretty Good Game Engine

Started life as libGDX 3D and Bullet physics demo from "http://bedroomcoders.co.uk/libgdx-bullet-redux-2/", 
modified to Ashley ECS (entity component system architecture).

Present Features:


Features TBD:
- "action" ... shoot or flip.
- jewels
- bullets
- camera auto-panning/circling when no player input
- let camera 3rd pers. mode be also controlled by input mapper
- for effect for polygon appearing/removing from view (put a "bubble" (opaque) around the player/camera position if this help diffuse)
- debug panel can enable/disable layers or groups of object/geometery for troubleshooting
- make "test screen" with  landscape mesh in gdxBulletRedux

Code Improvement
- RenderSystem multi-instance ModelComp?
- status system track charactre position deltas and broadcast event at intervals i.e. when given position delta exceeded
  ... characters to maintain list of last 10 characters and positions reported (and sorted by proximity)
  ... characters to set track on nearest, track as long or until proximity delta exceeded .. then reset track to
         current nearest.

Issues:
- x/N ... ("prize count" ... harcoded (get N by counting)
- make test objects conditional on bogus feature (on "test" Screen only)
- more wonky keys? from "pause", "resume" when target in-site, (should not "shoot") ... check "from Exit, (key down i.e. back-up into the exit panel) and key-down goes to menu-screen (preferences already hi-lited)
- crashes on screen teardown (windoze desktop only)
- bullet world entire game lifetime (experiment for windows crashes)
- rigs to use "nodes" model for all
- bumpy triangle meshes (fixed? tank had bumpy wheels ... adjusted origin/CG)
- wonky camera falling (apply more filter on trackerSB ) ... should be "lazier"


Models Used Info

APC Tank
Submitted by gabrielsdj (Personal Use License)
https://free3d.com/3d-model/apc-tank-42997.html
Reset tank w/ desired CG over grid center. Shift S 3D-curson to origin.
ctrl+shift+alt+c origin to 3D cursor
s .3 (.25?)  r x -90 ctrl+a s ctrl+a r ctrl+a location
r x 90
export FBX .01

RAM3500 by WTLion
https://www.turbosquid.com/3d-models/ram-3ds-free/491059
"free for personal and comercial use,requeriment only credits for author."
Import 3DS
adjust origin
s 1.75
r z 180
ctrl+a r
Export FBX Z-Forward Y-Up Scale 0.01

Tankcar
Author: atze
https://opengameart.org/content/tankcar
license for model "panzerwagen" is CC0.
License(s): CC0
s .5 Ctrl+a s
r z 180 Ctrl+a r
Export FBX Z-Forward Y-Up Scale 0.01


Military van
Author:  johndh
https://opengameart.org/content/military-van
License(s): CC-BY-SA 3.0 GPL 3.0 GPL 2.0
Export OBJ Z-Forward Y-Up Scale 0.35

3D Low poly offroad vehicle by nice3dmodels
https://www.turbosquid.com/3d-models/3d-offroad-car-1328883
Open offroadcar.blend
Export OBJ Z-Forward Y-Up Scale 0.35


Things to investigate:
ComponentMapper
IteratingSystem
PooledEngine
