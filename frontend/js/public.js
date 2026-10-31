"use strict";
const KEY='huellitas_v2';
const fallback=[
{id:'A-001',nombre:'Luna',especie:'Perro',edad:3,sexo:'Hembra',estado:'Disponible',descripcion:'Sociable y juguetona',foto:'assets/mascotas/luna.png'},
{id:'A-002',nombre:'Canela',especie:'Perro',edad:1,sexo:'Hembra',estado:'Disponible',descripcion:'Pequeña y curiosa',foto:'assets/mascotas/canela.png'},
{id:'A-003',nombre:'Oreo',especie:'Perro',edad:2,sexo:'Macho',estado:'Disponible',descripcion:'Cariñoso y tranquilo',foto:'assets/mascotas/oreo.png'},
{id:'A-004',nombre:'Michi',especie:'Gato',edad:2,sexo:'Hembra',estado:'Disponible',descripcion:'Dulce y observadora',foto:'assets/mascotas/michi.png'},
{id:'A-005',nombre:'Tortuguita',especie:'Otro',edad:2,sexo:'Hembra',estado:'Disponible',descripcion:'Busca un hogar responsable',foto:'assets/mascotas/tortuga.png'},
{id:'A-006',nombre:'Sol',especie:'Perro',edad:4,sexo:'Hembra',estado:'Disponible',descripcion:'Activa y alegre',foto:'assets/mascotas/sol.png'},]; 
function getData(){
 let d=null;try{d=JSON.parse(localStorage.getItem(KEY))}catch{}
 if(!d||!Array.isArray(d.animals)||!Array.isArray(d.adoptions)){
   d={animals:fallback.map(a=>({...a})),medical:[],vaccines:[],appointments:[],adoptions:[]};
 }else{
   d.animals=d.animals.filter(a=>a.id!=='A-007'&&a.nombre?.toLowerCase()!=='copito');
   // No reinsertar animales eliminados por el administrador.
   if(!localStorage.getItem('huellitas_catalogo_inicializado')){
     for(const pet of fallback){if(!d.animals.some(a=>a.id===pet.id))d.animals.push({...pet})}
   }
 }
 try{localStorage.setItem('huellitas_catalogo_inicializado','1');if(localStorage.getItem(KEY)!==JSON.stringify(d))localStorage.setItem(KEY,JSON.stringify(d))}catch{}
 return d;
}
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const grid=document.getElementById('animalGrid');const filter=document.getElementById('filter');const modal=document.getElementById('adoptModal');
function render(){const list=getData().animals.filter(a=>a.estado==='Disponible'&&(filter.value==='Todos'||a.especie===filter.value)).sort((a,b)=>Number(b.discapacidad==='Sí'||b.pronostico==='Tiempo limitado')-Number(a.discapacidad==='Sí'||a.pronostico==='Tiempo limitado'));grid.innerHTML=list.map(a=>`<article class="pet-card"><div class="pet-photo" aria-hidden="true">${/^(data:image\/|assets\/mascotas\/)/.test(a.foto||"")?`<img src="${esc(a.foto)}" alt="">`:a.especie==='Gato'?'🐱':a.especie==='Perro'?'🐶':'🐾'}</div><div class="pet-body"><div class="pet-head"><h3>${esc(a.nombre)}</h3><span class="tag">Disponible</span></div>${a.discapacidad==='Sí'||a.pronostico==='Tiempo limitado'?`<p class="priority-note">💚 Necesita atención especial${a.discapacidad==='Sí'?' · Discapacidad':''}${a.pronostico==='Tiempo limitado'?' · Pronóstico delicado':''}</p>`:''}<div class="pet-meta">${esc(a.especie)} · ${esc(a.edad)} ${Number(a.edad)===1?'año':'años'} · ${esc(a.sexo)}</div><p class="pet-description">${esc(a.descripcion||'Busca una familia amorosa.')}</p><button class="btn" data-animal="${esc(a.id)}" type="button">Quiero adoptarlo ♡</button></div></article>`).join('');document.getElementById('empty').hidden=list.length>0;grid.querySelectorAll('[data-animal]').forEach(b=>b.addEventListener('click',()=>openForm(b.dataset.animal)))}
function openForm(id){const a=getData().animals.find(x=>x.id===id&&x.estado==='Disponible');if(!a)return;document.getElementById('animalId').value=id;document.getElementById('modalTitle').textContent='Conoce a '+a.nombre;document.getElementById('formMessage').textContent='';modal.hidden=false;document.querySelector('[name=adoptante]').focus()}
function close(){modal.hidden=true;document.getElementById('adoptForm').reset()}
filter.addEventListener('change',render);document.getElementById('close').addEventListener('click',close);modal.addEventListener('click',e=>{if(e.target===modal)close()});document.addEventListener('keydown',e=>{if(e.key==='Escape'&&!modal.hidden)close()});
document.getElementById('adoptForm').addEventListener('submit',e=>{e.preventDefault();const form=e.currentTarget;const values=Object.fromEntries(new FormData(form));const data=getData();if(!data.animals.some(a=>a.id===values.animal&&a.estado==='Disponible')){document.getElementById('formMessage').textContent='Este animal ya no está disponible.';return}data.adoptions.push({...values,id:'D-'+Date.now().toString(36).toUpperCase(),fecha:new Date().toISOString().slice(0,10),estado:'En proceso'});try{localStorage.setItem(KEY,JSON.stringify(data));form.reset();document.getElementById('formMessage').textContent='¡Tu solicitud fue enviada con éxito! Nos estaremos comunicando contigo en un plazo de 2 a 4 días hábiles.'}catch(err){document.getElementById('formMessage').textContent='No se pudo guardar. Comprueba el almacenamiento del navegador.'}});
render();window.addEventListener('storage',e=>{if(e.key===KEY)render()});

// Recomendaciones: puntuación transparente basada en preferencias y características del animal.
const matchForm=document.getElementById('matchForm');
matchForm.addEventListener('submit',e=>{e.preventDefault();const v=Object.fromEntries(new FormData(matchForm));const animals=getData().animals.filter(a=>a.estado==='Disponible');const ranked=animals.map(a=>{let score=0;const reasons=[];if(v.especie==='Todos'||a.especie===v.especie){score+=35;reasons.push('tipo de mascota preferido')}if(!a.actividad||a.actividad===v.actividad){score+=20;reasons.push('nivel de actividad')}if(!a.vivienda||a.vivienda==='Ambos'||a.vivienda===v.vivienda){score+=15;reasons.push('tipo de hogar')}if(v.ninos==='No'||a.aptoNinos==='Sí'){score+=15;reasons.push('convivencia con niños')}if(v.otrasMascotas==='No'||a.aptoMascotas==='Sí'){score+=15;reasons.push('convivencia con mascotas')}return {a,score,reasons}}).sort((x,y)=>y.score-x.score);document.getElementById('matchResults').innerHTML=ranked.length?ranked.slice(0,3).map(({a,score,reasons})=>`<article class="pet-card"><div class="pet-photo">${/^(data:image\/|assets\/mascotas\/)/.test(a.foto||'')?`<img src="${esc(a.foto)}" alt="Foto de ${esc(a.nombre)}">`:'🐾'}</div><div class="pet-body"><h3>${esc(a.nombre)}</h3><p><strong>${score}% de coincidencia orientativa</strong></p><p>${esc(reasons.join(', '))}</p><button class="btn" type="button" data-recommend="${esc(a.id)}">Solicitar adopción</button></div></article>`).join(''):'<p>No hay animales disponibles.</p>';document.querySelectorAll('[data-recommend]').forEach(b=>b.onclick=()=>openForm(b.dataset.recommend));document.getElementById('matchResults').scrollIntoView({behavior:'smooth',block:'nearest'})});

window.addEventListener('focus',render);document.addEventListener('visibilitychange',()=>{if(!document.hidden)render()});
