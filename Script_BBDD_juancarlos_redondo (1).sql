create database proyectopokemon;
drop database if exists proyectopokemon;


create table Entrenador (

identrenador int not null auto_increment,
nombre varchar(20) not null,
edad int null,
primary key (identrenador)
);
insert into Entrenador
	values (1, "Alberto", 18),(2, "Benito" , 12),(3, "Rodrigo", 50),(4, "Maria", 26),(5, "Adriana", 15);
    
create table Pokemon (

numpokedex int not null auto_increment,
nompokemon varchar(20) not null,
primary key(numpokedex)
);
insert into Pokemon
	values (1,"Bulbasaur"),(2,"Ivysaur"),(3,"Venasaur"),(4,"Charmander"),(5,"Charmeleon");

create table Equipo(

codequipo int not null auto_increment,
identrenador int not null,
primary key(codequipo),
constraint fk_equipo_entrenador foreign key(identrenador) references Entrenador (identrenador)
		on update cascade


);
insert into Equipo
	values(1,3),(2,5),(3,1),(4,2),(5,4);


create table Region (

codregion int not null auto_increment,
nomregion varchar(20) not null,

primary key(codregion)
);
insert into Region
	values (1,"Kanto"),(2,"Jhoto"),(3,"Hoen"),(4,"Shinno"),(5,"Teselia");


create table Gimnasio (
codgym int not null auto_increment,
codregion int not null,
nomMedalla varchar(20) null,
tipoMedalla varchar(20) not null,

primary Key (codgym),
constraint fk_gimnasio_region foreign key (codregion) references Region (codregion)
	on update cascade


);
insert into Gimnasio
	values(1,1, "Ciudad Plateada","Roca"),(2,1, "Ciudad Celeste","Agua"),(3,1, "Ciudad Carmín","Electrico"),(4,1, "Ciudad Azulona","Planta"),(5,1, "Ciudad Fucsia","Veneno");

create table Medallas (

identrenador int not null,
codgym int not null,
fecha date null,

primary key(identrenador,codgym),
constraint fk_medallas_entrenador foreign key (identrenador) references Entrenador (identrenador)
	on update cascade,

constraint fk_medallas_gimnasio foreign key (codgym) references Gimnasio (codgym)
	on update cascade
);
insert into Medallas
	values(3,1,'2008-03-21'),(3,5,'2018-05-20'),(3,2,'2010-06-27'),(5,4,'2023-01-11'),(2,1,'2025-11-09');

create table Tipo(
codtipo int not null auto_increment,
nomtipo varchar(10) not null,

primary key (codtipo)
);
insert into Tipo
	values (1,"Planta"),(2,"Fuego"),(3,"Agua"),(4,"Veneno"),(5,"Volador");
create table DetallePokemon (
numpokedex int not null,
codtipo int not null,
primary key(numpokedex,codtipo),
constraint fk_detalletipo_pokemon foreign key (numpokedex) references Pokemon (numpokedex)
	on update cascade,

constraint fk_detalletipo_tipo foreign key (codtipo) references Tipo (codtipo)
	on update cascade


);
insert into DetallePokemon
	values (1,1),(2,1),(3,1),(3,4),(4,2);
drop table detalleEquipo;
create table DetalleEquipo(
codequipo int not null,
numslot int,
numpokedex int not null,

primary key(codequipo,numslot),
constraint fk_detalleequipo_equipo foreign key(codequipo) references Equipo (codequipo)
	on update cascade,
constraint fk_detalle_pokemon foreign key (numpokedex) references Pokemon (numpokedex)
	on update cascade,
 check (numslot between 1 and 6)

);

insert into DetalleEquipo
	values(1,1,1),(1,2,3),(2,1,5),(2,2,4),(3,1,2);
    
   
		
