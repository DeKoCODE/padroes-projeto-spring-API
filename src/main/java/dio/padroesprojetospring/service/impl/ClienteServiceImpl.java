package dio.padroesprojetospring.service.impl;

import dio.padroesprojetospring.model.Cliente;
import dio.padroesprojetospring.model.ClienteRepository;
import dio.padroesprojetospring.model.Endereco;
import dio.padroesprojetospring.model.EnderecoRepository;
import dio.padroesprojetospring.service.ClienteService;
import dio.padroesprojetospring.service.ViaCepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {
    //Injetados componentes do Spring com @Autowired.
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private ViaCepService viaCepService;

    //Strategy: Implementados os métodos definidos na interface.
    //Facade: Abstrair integrações com subsistemas, provendo interface.
    @Override
    public Iterable<Cliente> buscarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente buscarPorId(Long id) {
        //Verifica se o endereço do cliente já existe, pelo CEP.
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.get();
    }

    @Override
    public void inserir(Cliente cliente) {
        //Verifica se o endereço do cliente ja existe, pelo CEP.
        salvarClienteComCep(cliente);
    }

    @Override
    public void atualizar(Long id, Cliente cliente) {
        //Busca cliente por ID, caso exista.
        Optional<Cliente> clienteBd = clienteRepository.findById(id);
        if (clienteBd.isPresent()){
            //Verifica se o endereço do cliente já existe, pelo Cep.
            //Caso não exista, integrar com ViaCep e persistir o retorno.
            //Altera cliente vinculando o endereço, novo ou existente.
            salvarClienteComCep(cliente);
        }
    }

    @Override
    public void deletar(Long id) {
        clienteRepository.deleteById(id);
    }

    private void salvarClienteComCep(Cliente cliente) {
        //Verifica se o endereço do cliente já existe, pelo Cep.
        String cep = cliente.getEndereco().getCep();
        Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
            //Caso não exista, integra com ViaCEP e persiste o retorno.
            Endereco novoEndereco = viaCepService.consultarCep(cep);
            enderecoRepository.save(novoEndereco);
            return novoEndereco;
        });
        cliente.setEndereco(endereco);
        //Insere cliente vinculando ao endereço, novo ou existente.
        clienteRepository.save(cliente);
    }
}
