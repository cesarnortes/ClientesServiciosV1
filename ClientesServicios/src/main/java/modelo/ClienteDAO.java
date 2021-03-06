package modelo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//LAS IMPORTACIONES SE HACEN AUTOMATICAMENTE SIEMPRE QUE EL PROYECTO TENGA LA LIBRERIA NECESARIA CARGADA
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dominio.Cliente;
import dominio.Servicio;

//@Repository indica a Spring que esta clase contiene los metodo necesarios para operar con la base de datos
//@Transactional indica que los metodos de esta clase debe tratarse como transaciones
//donde en caso de haber varias opraciones en cadena si una falla deshiciera las demas operacioes anteriores
//para no dejar una operacion grande a medias, en resumen o se hace todo o no se hace nada
@Repository
@Transactional
public class ClienteDAO implements ClienteDAOInterface {
//implementamos siempre una interfaz con los metodos CRUD en los dao
	////////////////////////////////////////////////////////////////
//Esta clase sera un bean que creara spring desde si archivo de configuracion
//todo clase que pueda tratarse como un bean (Clase POJO de java) y para ello es 
//necesario tener obviamente sus atributos (Variables) y los getter y setter para la obtencion y establecimiento 
//de las variables de dicha clase y un contructor sin parametros
//por defecto todas las clases heredan un contructor o puedes crearlo
	///////////////////////////////////////////////////////////////

	// esta variable cargara los datos necesarios en el archivo de configuracion
	// matiendo el codigo fuente del dao lo mas independiente posible
	// sobre el tipo de base de datos, su usuario, su password, su direccion, sea
	// cual sea los datos de configuracion
	// se ponen aparte para no alterar un codigo fuente de calidad
	JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbctemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbctemplate) {
		this.jdbcTemplate = jdbctemplate;
	}

	///////////////////////////////////////////////////////////////////

	public boolean alta(Cliente c) {

		// En SpringJDBC debemos encerrar la ejecucion de jdbcTemplate en try cath
		// solo lo ponemos en el alta
		try {

			// creamos la sentencia sql que ejecurara jdbctemplate
			// el ? ocuparÃ¡ el valor de la variable que especifiquemos como parametro en
			// jdbcTemplate.update
			// el metodo .update se usa para ejecutar sentencias SQL que modifiquen datos de
			// una tabla ya sea
			// borrar, insertar o modificar

			String sentenciaSQL = "INSERT INTO tclientes VALUES(null,?,?)";

			// pasamos como parametro el valor de ?
			jdbcTemplate.update(sentenciaSQL, c.getIdCliente(), c.getNombre(), c.getTelefono());

		} catch (DataAccessException e) {
			// usamos DataAccessException como clase a la hora de capturar las excepciones
			// de acceso a datos en este caso
			// y se una excepciÃ³n salta devolvemos el false para indicar el error del
			// metodo
			return false;

		}
		// devolvemos true si todo sucede satisfactoriamente
		return true;

	}

	public void baja(int idCliente) {

		// DE IGUAL MANERA CREAMOS UNA SENTENCIA SQL PARA DAR DE BAJA UN CLIENTE
		// ANTONIO DIJO QUE AL BORRAR UN CLIENTE HAY QUE DAR PRIMERO DE BAJA LOS
		// REGISTRO DE LAS TABLAS DEPENDIENTES
		// DE LA ENTIDAD FUERTE EN ESTE CASO AL CREAR UN SERVICIO (ENTIDAD DEBIL) ES
		// OBLIGATORIO QUE TENGA UN CLIENTE YA QUE EN LA TABLA
		// LOS SERVICIOS TIENEN UNA CLAVE FORANEA (UN ID DE CLIENTE) Y PARA EVITAR
		// ERRORES EN LA BASE DE DATOS DE LA INTEGRIDAD REFERENCIAL
		// HAY QUE DAR DE BAJA PRIMERO LAS ENTIDADES DEBILES PERO YO HE PREFERIDO DEJAS
		// QUE ESO SE HAGA EN LA CLASE MODELO
		// Y QUE LOS DAO DE LAS ENTIDADES TANSOLO MANEJEN LOS METODOS CRUD DE LA ENTIDAD
		// Y EN MODELO.JAVA AHI DESARROLLO LA LOGIGA DE LOS METODOS
		// QUE OPERAN EN LA BASE DE DATOS
		String sentenciaSQL = "DELETE * FROM tclientes WHERE IdCliente=?";

		// pasamos como parametro el valor de ?
		jdbcTemplate.update(sentenciaSQL, idCliente);

	}

	public void modificacion(Cliente c) {

		// se llama al mismo metodo que la alta .update pero la sentencia sql sera
		// distinta
		String sql = "update tclientes set Nombre=?, Telefono=? where IdCliente=?";
		jdbcTemplate.update(sql, c.getNombre(), c.getTelefono(), c.getIdCliente());

	}

	// el parametro readOnly=true de la anotacion indica que el metodo
	// solo va a leer información de la base de datos
	@Transactional(readOnly = true)
	public Cliente consulta(int idCliente) {
		// TODO Auto-generated method stub
		String sql = "select * from tclientes WHERE IdCliente=?";
		// en new Object[] { idCliente } especificamos el valor del parametro de
		// IdCliente=?
		Cliente cliente = (Cliente) jdbcTemplate.queryForObject(sql, new Object[] { idCliente }, new ClienteMapper());
		return cliente;
	}

	@Transactional(readOnly = true)
	public List<Cliente> consultaAll() {
		String sql = "select * from tclientes";

		// jdbcTemplate.query devulve un objeto List<> del tipo de objeto que creas
		List<Cliente> todosClientes = jdbcTemplate.query(sql, new ClienteMapper());
		return todosClientes;
	}

	///////////////////////////////////////////////////////////////////////

	// Mapper
	// Los mapper son la clase que hay que desarrollar donde especificamos que
	// columna corresponde con con que variable
	// del objeto que queremos contruir y finalmente devuelve un objero del tipo
	// especificado
	// siempre que que haga una consulta hay que pasarle por parametro al metodo de
	// consulta de jdbcTemplate
	// un mapper del objeto que deseas consultar

	class ClienteMapper implements RowMapper<Cliente> {
		public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {

			// creamos el objeto
			Cliente cliente = new Cliente();

			// asignamos el valor extraido en el resulset en sus variables con los setter
			cliente.setIdCliente(rs.getInt("IdCliente"));
			cliente.setNombre(rs.getString("Nombre"));
			cliente.setTelefono("Telefono");

			// cuado el objeto tiene un array de otro objeto u otro objeto a secas
			// almacenado en otra tabla
			// hay que hacer una consulta para extraer los registros necesarios
			String sql = "Select * from TServicios where idCliente=?";
			List<Servicio> servicio = jdbcTemplate.query(sql, new Object[] { cliente.getIdCliente() },
					new ServicioMapper());
			// como se puede ver queremos un array de los servicios del cliente entonces al
			// crear un cliente
			// necesitamos cargar todos los servicios del mismo modo de siempre

			cliente.setListaServicios((ArrayList<Servicio>) servicio);

			return cliente;
		}
	}

	class ServicioMapper implements RowMapper<Servicio> {
		public Servicio mapRow(ResultSet rs, int rowNum) throws SQLException {
			String sql = "select * from TClientes where IdCliente=? ";
			int idCliente = rs.getInt("IdCliente");
			Servicio servicio = new Servicio();

			servicio.setIdServicio(rs.getInt("IdServicio"));
			servicio.setDescripcion(rs.getString("Concepto"));
			servicio.setFecha(rs.getDate("Fecha"));
			// este maper de servicio es exactamente igual solo que no necesita un array
			// solo un cliente especifico
			// consultamos el cliente con jdbcTemplate.queryForObject para especificar que
			// la busqueda
			// solo trae un objeto o registro, de la mis forma pasamos el mapper del objeto
			// que debe crear
			// y asi ya usaria el codigo la aplicacion de manera recurrente una y otra vez
			// segun objetos y objetos anidados
			// tenga que crear
			servicio.setCliente(jdbcTemplate.queryForObject(sql, new Object[] { idCliente }, new ClienteMapper()));

			return servicio;

		}
	}

}
